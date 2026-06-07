# ERD

## 개요

Prompt-to-Animation Generator는 사용자의 자연어 프롬프트를 기반으로 Scene, Cut, Image, Video를 생성하여 최종 애니메이션을 제작하는 서비스입니다.

---

## 엔티티 관계

text Generation : Scene = 1 : 1  Scene : Cut = 1 : N  Cut : CutImage = 1 : N  Cut : CutVideo = 1 : N  CutImage : CutVideo = 1 : N

---

## Generation

사용자의 애니메이션 생성 요청 정보를 저장합니다.

### 컬럼

| 컬럼명 | 설명 |
|---------|---------|
| generationId | 생성 작업 ID |
| userPrompt | 사용자 프롬프트 |
| status | 생성 상태 |
| resultUrl | 최종 병합 영상 URL |
| errorMessage | 생성 실패 메시지 |
| createdAt | 생성 요청 시각 |
| completedAt | 생성 완료 시각 |

### 상태값

text PENDING PROCESSING COMPLETED FAILED TIMEOUT

### 설계 의도

사용자의 애니메이션 생성 작업 단위를 표현합니다.

애니메이션 생성 과정 전체를 추적하기 위한 최상위 엔티티입니다.

---

## Scene

사용자 프롬프트를 기반으로 생성된 장면 정보를 저장합니다.

### 컬럼

| 컬럼명 | 설명 |
|---------|---------|
| sceneId | 장면 ID |
| generationId | 생성 작업 ID |
| title | 장면 제목 |
| scenario | 장면 시나리오 |

### 관계

text Generation 1 : 1 Scene

### 설계 의도

하나의 생성 요청은 하나의 Scene을 생성합니다.

Scene은 여러 개의 Cut을 생성하기 위한 스토리보드 역할을 수행합니다.

---

## Cut

Scene을 구성하는 세부 장면 정보를 저장합니다.

### 컬럼

| 컬럼명 | 설명 |
|---------|---------|
| cutId | 컷 ID |
| sceneId | 장면 ID |
| cutOrder | 컷 순서 |
| imagePrompt | 이미지 생성 프롬프트 |
| videoPrompt | 비디오 생성 프롬프트 |
| durationSec | 컷 길이(초) |

### 관계

text Scene 1 : N Cut

### 설계 의도

하나의 Scene은 여러 개의 Cut으로 구성됩니다.

각 Cut은 독립적으로 이미지 및 비디오를 생성할 수 있습니다.

---

## CutImage

Cut 기반으로 생성된 이미지 정보를 저장합니다.

### 컬럼

| 컬럼명 | 설명 |
|---------|---------|
| cutImageId | 컷 이미지 ID |
| cutId | 컷 ID |
| status | 이미지 생성 상태 |
| imageUrl | 생성 이미지 URL |
| errorMessage | 이미지 생성 실패 메시지 |
| createdAt | 이미지 생성 시각 |

### 관계

text Cut 1 : N CutImage

### 설계 의도

현재 MVP에서는 Cut당 이미지 1개를 생성하지만, 향후 이미지 재생성 또는 다중 후보 이미지 생성을 고려하여 1:N 관계로 설계하였습니다.

---

## CutVideo

Cut 및 CutImage를 기반으로 생성된 비디오 정보를 저장합니다.

### 컬럼

| 컬럼명 | 설명 |
|---------|---------|
| cutVideoId | 컷 비디오 ID |
| cutId | 컷 ID |
| cutImageId | 생성에 사용된 컷 이미지 ID |
| status | 비디오 생성 상태 |
| videoUrl | 생성 비디오 URL |
| errorMessage | 비디오 생성 실패 메시지 |
| createdAt | 비디오 생성 시각 |

### 관계

text Cut 1 : N CutVideo  CutImage 1 : N CutVideo

### 설계 의도

현재 MVP에서는 Cut당 비디오 1개를 생성하지만, 향후 비디오 재생성 또는 다중 후보 비디오 생성을 고려하여 확장 가능한 구조로 설계하였습니다.

CutVideo는 특정 Cut에 속하면서 동시에 특정 CutImage를 기반으로 생성됩니다.

생성된 CutVideo는 최종 애니메이션 생성 시 병합 대상이 됩니다.