# CLAUDE.md

## Project Overview

Prompt-to-Animation Generator

사용자 프롬프트를 Scene으로 변환하고 Cut 단위 이미지 및 비디오 생성 과정을 통해 애니메이션을 생성하는 서비스입니다.

## Tech Stack

### Backend

- Java 17
- Spring Boot
- Spring Data JPA
- PostgreSQL

## Architecture

Layered Architecture

- controller
- service
- repository
- domain
- dto
- client
- config
- exception

## Domain

- Generation : Scene = 1 : 1
- Scene : Cut = 1 : N
- Cut : CutImage = 1 : N
- Cut : CutVideo = 1 : N

## API

- POST /api/generations
    - 애니메이션 생성 요청
- GET /api/generations/{generationId}
    - 생성 상태 조회
- GET /api/generations/{generationId}/result
    - 생성 결과 조회

## Response Format

### Success
```json
{
    "success": true,
    "message": "요청 처리 성공",
    "errorCode": null,
    "data": {}
}
```
### Error
```json
{
    "success": false,
    "message": "에러 메시지",
    "errorCode": "ERROR_CODE",
    "data": null
}
```

## Validation

- userPrompt 필수
- userPrompt 1~500자

## Error Handling

Use a global exception handler and return errors in the common response format.

### Common Error Codes

#### Validation
- PROMPT_REQUIRED: 프롬프트는 필수입니다.
- PROMPT_TOO_LONG: 프롬프트는 500자를 초과할 수 없습니다.

#### Generation
- INVALID_GENERATION_ID: 생성 작업 ID가 올바르지 않습니다.
- GENERATION_NOT_FOUND: 생성 작업을 찾을 수 없습니다.
- GENERATION_NOT_COMPLETED: 생성이 완료된 후 결과를 조회할 수 있습니다.

#### Rate Limit
- TOO_MANY_REQUESTS: 요청 횟수를 초과했습니다.

#### Internal Server Error
- GENERATION_CREATE_FAILED: 생성 작업 생성에 실패했습니다.
- GENERATION_STATUS_FETCH_FAILED: 생성 상태 조회에 실패했습니다.
- GENERATION_RESULT_FETCH_FAILED: 생성 결과 조회에 실패했습니다.

## Rules

- API Key는 Backend 환경변수로만 관리
- OpenAI API Key를 코드에 하드코딩하지 않음
- Kie API Key를 코드에 하드코딩하지 않음
- MVP를 우선 구현
- 공통 응답 포맷 사용
- 공통 예외 처리 적용
- JPA 사용
- PostgreSQL 사용