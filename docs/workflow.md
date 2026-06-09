# Workflow

## 개요

Prompt-to-Animation Generator는 사용자의 자연어 프롬프트를 입력받아 Scene, Cut, Image, Video를 생성한 뒤 최종 애니메이션을 반환하는 서비스입니다.

---

# 전체 흐름

```text
사용자 프롬프트 입력
        ↓
Generation 생성 (PENDING) → ID 즉시 반환
        ↓  [TransactionalEventListener AFTER_COMMIT + @Async]
Generation 상태 변경 (PROCESSING)
        ↓
Scene 생성 + Cut 생성 (OpenAI GPT-5.4 mini)
        ↓
Cut Image 생성 (Nano Banana) — 컷별 순차 처리, 실패 시 해당 컷만 FAILED
        ↓
Cut Video 생성 (Kling-2.6) — COMPLETED 이미지 있는 컷만 생성, 없으면 FAILED CutVideo 저장
        ↓
Cut Video 병합 (COMPLETED 영상만 대상)
        ↓
최종 영상 URL 저장
        ↓
Generation 상태 변경 (COMPLETED)
```

각 단계 사이에는 3초 딜레이(`STEP_DELAY_MS = 3000ms`)가 적용됩니다.

---

# 1. 애니메이션 생성 요청

## API

```http 
POST /api/generations
```

## 처리 순서

1. userPrompt를 검증합니다.
2. Generation을 PENDING 상태로 저장합니다.
3. Generation ID를 반환합니다.
4. `GenerationCreatedEvent`를 발행합니다.
5. 트랜잭션 커밋 후 `@Async("workflowExecutor")`로 비동기 워크플로우가 시작됩니다.

## 생성 직후 상태

```text 
PENDING
```

---

# 2. Scene 생성 + Cut 생성

Scene 생성과 Cut 생성은 하나의 단계(`generateScene`)에서 함께 처리됩니다.

## 사용 모델

```text 
GPT-5.4 mini
```

## 입력

```text 
사용자 프롬프트
```

예시

```text 
숲 속에서 곰이 꿀을 먹는 30초 애니메이션을 만들어줘.
```

## 출력

```json 
{
  "title": "꿀을 찾아 떠난 곰",
  "scenario": "곰이 숲 속에서 벌집을 발견하고 꿀을 먹으며 행복해하는 이야기",
  "style": "children's animation style, soft pastel colors, warm lighting",
  "cuts": [
    {
      "cutOrder": 1,
      "imagePrompt": "곰이 숲 속을 걷다가 벌집을 발견하는 장면",
      "videoPrompt": "곰이 벌집을 바라보며 천천히 다가가는 장면",
      "durationSec": 8
    }
  ]
}
```

## 처리 순서

1. OpenAI에 프롬프트를 전달합니다.
2. 응답의 `title`, `scenario`로 Scene을 저장합니다.
3. 응답의 `style`을 각 Cut의 `imagePrompt`, `videoPrompt` 앞에 prepend합니다.
   - 예: `"children's animation style, ..., " + imagePrompt`
4. `durationSec`을 클램핑합니다: `≤ 7초 → 5초`, `> 7초 → 10초` (Kie API 지원 범위)
5. 각 Cut을 저장합니다.

## 저장 정보 (Cut)

- cutOrder
- imagePrompt (style prepend 포함)
- videoPrompt (style prepend 포함)
- durationSec (클램핑 적용값)

---

# 3. Cut Image 생성

## 사용 모델

```text 
Nano Banana
```

## 처리 순서

1. Cut을 `cutOrder` 오름차순으로 순회합니다.
2. 각 Cut에 대해 CutImage를 PROCESSING 상태로 저장합니다.
3. Cut의 `imagePrompt`로 이미지 생성을 요청합니다.
4. 성공 시 CutImage를 COMPLETED 상태로 저장하고 imageUrl을 기록합니다.
5. 실패 시 CutImage를 FAILED 상태로 저장하고 나머지 컷 처리를 계속합니다.

---

# 4. Cut Video 생성

## 사용 모델

```text 
Kling-2.6
```

## 처리 순서

1. Cut을 `cutOrder` 오름차순으로 순회합니다.
2. 각 Cut에서 가장 최근 COMPLETED CutImage를 조회합니다.
3. COMPLETED CutImage가 없으면 FAILED CutVideo를 저장하고 다음 컷으로 넘어갑니다.
4. COMPLETED CutImage가 있으면 CutVideo를 PROCESSING 상태로 저장합니다.
5. Cut의 `videoPrompt`, CutImage의 `imageUrl`, Cut의 `durationSec`으로 영상 생성을 요청합니다.
6. 성공 시 CutVideo를 COMPLETED 상태로 저장하고 videoUrl을 기록합니다.
7. 실패 시 CutVideo를 FAILED 상태로 저장하고 나머지 컷 처리를 계속합니다.

---

# 5. 영상 병합

## 처리 순서

1. Cut을 `cutOrder` 오름차순으로 순회합니다.
2. 각 Cut에서 가장 최근 COMPLETED CutVideo의 URL을 수집합니다.
   - COMPLETED CutVideo가 없는 컷이 있으면 예외 발생 → Generation FAILED 처리
3. 수집된 videoUrl 목록을 FFmpeg으로 순서대로 병합합니다.
4. 최종 영상 URL을 Generation에 저장합니다.
5. Generation 상태를 COMPLETED로 변경합니다.

---

# 6. 생성 상태 조회

## API

```http 
GET /api/generations/{generationId}
```

## 상태값

```text 
PENDING | PROCESSING | COMPLETED | FAILED | TIMEOUT
```

## 진행 단계

```text 
SCENE_GENERATION
CUT_IMAGE_GENERATION
CUT_VIDEO_GENERATION
VIDEO_MERGE
COMPLETED
FAILED
```

---

# 7. 생성 결과 조회

## API

```http 
GET /api/generations/{generationId}/result
```

## 조회 가능 상태

COMPLETED, FAILED, TIMEOUT 상태에서 조회 가능합니다. PENDING, PROCESSING 상태에서는 조회 불가합니다.

## 반환 정보

### Scene

- title
- scenario

### Cuts

- cutOrder
- durationSec
- imagePrompt
- imageUrl
- videoPrompt
- videoUrl

### Result

- resultUrl

---

# 예외 처리

## 생성 실패

생성 과정 중 오류가 발생하면

```text 
Generation.status = FAILED
```

로 변경합니다.

실패 원인은

```text 
Generation.errorMessage
```

에 저장합니다.

## 타임아웃

생성 작업이 타임아웃되면

```text 
Generation.status = TIMEOUT
```

으로 변경합니다. TIMEOUT은 FAILED와 동일한 종료 상태로, 생성 결과 조회가 가능합니다.
