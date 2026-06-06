package org.sieun.prompt2animation.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Validation
    PROMPT_REQUIRED(HttpStatus.BAD_REQUEST, "프롬프트는 필수입니다."),
    PROMPT_TOO_LONG(HttpStatus.BAD_REQUEST, "프롬프트는 500자를 초과할 수 없습니다."),
    INVALID_GENERATION_ID(HttpStatus.BAD_REQUEST, "생성 작업 ID가 올바르지 않습니다."),

    // Generation
    GENERATION_NOT_FOUND(HttpStatus.NOT_FOUND, "생성 작업을 찾을 수 없습니다."),
    GENERATION_NOT_COMPLETED(HttpStatus.CONFLICT, "생성이 완료된 후 결과를 조회할 수 있습니다."),

    // Rate Limit
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "요청 횟수를 초과했습니다."),

    // Internal Server Error
    GENERATION_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "생성 작업 생성에 실패했습니다."),
    GENERATION_STATUS_FETCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "생성 상태 조회에 실패했습니다."),
    GENERATION_RESULT_FETCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "생성 결과 조회에 실패했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
