package org.sieun.prompt2animation.dto.response;

public record ApiResponse<T>(
        boolean success,
        String message,
        String errorCode,
        T data
) {
    private static final String DEFAULT_SUCCESS_MESSAGE = "요청 처리 성공";

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