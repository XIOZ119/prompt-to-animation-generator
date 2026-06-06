# Workflow

## 개요

Prompt-to-Animation Generator는 사용자의 자연어 프롬프트를 입력받아 Scene, Cut, Image, Video를 생성한 뒤 최종 애니메이션을 반환하는 서비스입니다.

---

# 전체 흐름

```text
사용자 프롬프트 입력
        ↓
Generation 생성 (PENDING)
        ↓
Generation 상태 변경 (PROCESSING)
        ↓
Scene 생성 (OpenAI GPT-5.4 mini)
        ↓
Cut 생성
        ↓
Cut Image 생성 (Nano Banana)
        ↓
Cut Video 생성 (Kling-2.6)
        ↓
Cut Video 병합
        ↓
최종 영상 URL 저장
        ↓
Generation 상태 변경 (COMPLETED)
```
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
4. 비동기 생성 작업을 시작합니다.

## 생성 직후 상태

```text 
PENDING
```

---

# 2. Scene 생성

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
  "cuts": [
    ...
  ]
}
```

## 처리 순서

1. OpenAI에 프롬프트를 전달합니다.
2. Scene을 생성합니다.
3. Scene 정보를 저장합니다.
4. Cut 정보를 저장합니다.

---

# 3. Cut 생성

Scene을 기반으로 여러 개의 Cut을 생성합니다.

예시

```text 
Cut1
곰이 숲을 걷는다

Cut2
벌집을 발견한다

Cut3
꿀을 먹는다

Cut4
행복해한다
```

## 저장 정보

- cutOrder
- imagePrompt
- videoPrompt
- durationSec

---

# 4. Cut Image 생성

## 사용 모델

```text 
Nano Banana
```

## 처리 순서

1. Cut의 imagePrompt를 사용합니다.
2. 이미지 생성 요청을 보냅니다.
3. 생성된 이미지 URL을 저장합니다.
4. CutImage 상태를 저장합니다.

---

# 5. Cut Video 생성

## 사용 모델

```text 
Kling-2.6
```

## 처리 순서

1. Cut의 videoPrompt를 사용합니다.
2. 생성된 CutImage를 입력으로 사용합니다.
3. 비디오 생성 요청을 보냅니다.
4. 생성된 비디오 URL을 저장합니다.
5. CutVideo 상태를 저장합니다.

---

# 6. 영상 병합

## 처리 순서

1. Cut Video를 순서대로 조회합니다.
2. 모든 비디오를 병합합니다.
3. 최종 애니메이션을 생성합니다.
4. 최종 영상 URL을 저장합니다.

---

# 7. 생성 상태 조회

## API

```http 
GET /api/generations/{generationId}
```

## 상태값

```text 
PENDING
PROCESSING
COMPLETED
FAILED
TIMEOUT
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

# 8. 생성 결과 조회

## API

```http 
GET /api/generations/{generationId}/result
```

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

---

# 구현 우선순위

1. Generation 생성 API
2. Generation 상태 조회 API
3. Generation 결과 조회 API
4. Mock 생성 워크플로우
5. OpenAI 연동
6. Kie Image 연동
7. Kie Video 연동
8. 영상 병합 구현
9. 프론트엔드 구현