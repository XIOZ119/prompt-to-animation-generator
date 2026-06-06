package org.sieun.prompt2animation.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {

    private static final String DEFAULT_SUCCESS_MESSAGE = "요청 처리 성공";

    private final boolean success;
    private final String message;
    private final String errorCode;
    private final T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, DEFAULT_SUCCESS_MESSAGE, null, data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, null, data);
    }

    public static ApiResponse<Void> error(String message, String errorCode) {
        return new ApiResponse<>(false, message, errorCode, null);
    }
}
