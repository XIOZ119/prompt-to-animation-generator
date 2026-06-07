# API 명세서

# 1. 애니메이션 생성

## 기본 정보

| 항목 | 내용 |
| --- | --- |
| 카테고리 | Generation |
| Method | POST |
| URL | /api/generations |
| 설명 | 사용자 프롬프트를 기반으로 애니메이션 생성 작업 시작 |

## Request

### Body

| key | type | required | description |
| --- | --- | --- | --- |
| userPrompt | String | Y | 애니메이션으로 만들 사용자 자연어 프롬프트 (1~500자) |

### Request Example

```http 
POST /api/generations 
Content-Type: application/json  
{   
    "userPrompt": "숲 속에서 곰이 꿀을 먹는 30초 애니메이션을 만들어줘." 
}
```

## Response

| key | type | 설명 |
| --- | --- | --- |
| generationId | Long | 생성 작업 ID |
| status | String | 생성 상태 |
| createdAt | String | 생성 시각 |

### Response Example

```json
{
  "success": true,   
  "message": "애니메이션 생성 요청 성공",   
  "errorCode": null,   
  "data": {     
    "generationId": 1,     
    "status": "PENDING",     
    "createdAt": "2026-06-04T15:00:00"
  }
}
```

## 비즈니스 로직

1. userPrompt를 검증합니다.
2. Generation을 PENDING 상태로 생성합니다.
3. Generation ID를 반환합니다.
4. 비동기 생성 프로세스를 시작합니다.
5. Generation 상태를 PROCESSING으로 변경합니다.
6. Scene → Cut → Image → Video → 영상 병합 과정을 수행합니다.
7. 성공 시 COMPLETED 상태로 변경합니다.
8. 실패 시 FAILED 상태로 변경합니다.

---

# 2. 생성 상태 조회

## 기본 정보

| 항목 | 내용 |
| --- | --- |
| 카테고리 | Generation |
| Method | GET |
| URL | /api/generations/{generationId} |
| 설명 | 애니메이션 생성 진행 상태 조회 |

## Request

### Path Variable

| key | type | required | description |
| --- | --- | --- | --- |
| generationId | Long | Y | 생성 작업 ID |

### Request Example

```http 
GET /api/generations/1
```

## Response

| key | type | 설명 |
| --- | --- | --- |
| generationId | Long | 생성 작업 ID |
| status | String | 생성 상태 |
| progress | Integer | 생성 진행률 |
| currentStep | String | 현재 진행 단계 |
| currentStepMessage | String | 화면에 표시할 현재 진행 메시지 |
| completedStepCount | Integer | 완료된 단계 수 |
| totalStepCount | Integer | 전체 단계 수 |
| errorMessage | String | 실패 메시지 |

### Response Example

```json 
{   
  "success": true,   
  "message": "생성 상태 조회 성공",   
  "errorCode": null,   
  "data": {     
    "generationId": 1,     
    "status": "PROCESSING",     
    "progress": 50,     
    "currentStep": "CUT_VIDEO_GENERATION",     
    "currentStepMessage": "컷 3 비디오 생성 중...",     
    "completedStepCount": 7,     
    "totalStepCount": 14,     
    "errorMessage": null
  }
}
```

### currentStep

| currentStep | 화면 표시 |
| --- | --- |
| SCENE_GENERATION | Scene 생성 중 |
| CUT_IMAGE_GENERATION | Cut Image 생성 중 |
| CUT_VIDEO_GENERATION | Cut Video 생성 중 |
| VIDEO_MERGE | 최종 영상 병합 중 |
| COMPLETED | 완료 |
| FAILED | 실패 |

## 비즈니스 로직

1. generationId를 검증합니다.
2. Generation을 조회합니다.
3. 생성 상태를 반환합니다.
4. 현재 진행 단계 정보를 반환합니다.
5. 진행률 정보를 반환합니다.
6. 프론트는 Polling 방식으로 상태를 조회합니다.

---

# 3. 생성 결과 조회

## 기본 정보

| 항목 | 내용 |
| --- | --- |
| 카테고리 | Generation |
| Method | GET |
| URL | /api/generations/{generationId}/result |
| 설명 | 생성된 Scene, Cut, Video 정보 조회 |

## Request

### Path Variable

| key | type | required | description |
| --- | --- | --- | --- |
| generationId | Long | Y | 생성 작업 ID |

### Request Example

```http 
GET /api/generations/1/result
```

## Response

| key | type | 설명 |
| --- | --- | --- |
| generationId | Long | 생성 작업 ID |
| resultUrl | String | 최종 병합 영상 URL |
| scene | SceneResponse | 생성된 장면 정보 |
| cuts | List<CutResponse> | 컷별 생성 결과 |

### SceneResponse

| key | type | 설명 |
| --- | --- | --- |
| title | String | 장면 제목 |
| scenario | String | 장면 시나리오 |

### CutResponse

| key | type | 설명 |
| --- | --- | --- |
| cutOrder | Integer | 컷 순서 |
| durationSec | Integer | 컷 길이 |
| imagePrompt | String | 이미지 생성 프롬프트 |
| imageUrl | String | 컷 이미지 URL |
| videoPrompt | String | 비디오 생성 프롬프트 |
| videoUrl | String | 컷 비디오 URL |

### Response Example

```json 
{   
  "success": true,   
  "message": "생성 결과 조회 성공",   
  "errorCode": null,   
  "data": {     
    "generationId": 1,     
    "resultUrl": "https://example.com/final-animation.mp4",     
    "scene": {       
      "title": "꿀을 찾아 떠난 곰",       
      "scenario": "곰이 숲속에서 벌집을 발견하고 꿀을 먹으며 행복해하는 이야기"
    },     
    "cuts": [       
      {         
        "cutOrder": 1,         
        "durationSec": 5,         
        "imagePrompt": "곰이 숲 속을 걷다가 나무에 매달린 벌집을 발견하는 장면",         
        "imageUrl": "https://example.com/cut1.png",         
        "videoPrompt": "곰이 벌집을 바라보며 천천히 다가가는 장면",         
        "videoUrl": "https://example.com/cut1.mp4"
      }     
    ]
  }
}
```

## 비즈니스 로직

1. generationId를 검증합니다.
2. Generation을 조회합니다.
3. Generation 상태가 COMPLETED인지 확인합니다.
4. Scene 정보를 조회합니다.
5. Cut 정보를 조회합니다.
6. 최종 영상 URL을 반환합니다.
7. 컷별 생성 결과를 반환합니다.