package org.sieun.prompt2animation.util;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

@Slf4j
public class RetryUtil {

    private RetryUtil() {}

    public static <T> T execute(int maxAttempts, long baseDelayMs, String operationName, Supplier<T> action) {
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return action.get();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    long delay = baseDelayMs * (long) Math.pow(2, attempt - 1);
                    log.warn("[Retry] {} 실패 - {}/{}회, {}ms 후 재시도. error: {}",
                            operationName, attempt, maxAttempts, delay, e.getMessage());
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    log.error("[Retry] {} 최대 재시도 횟수({}) 초과. error: {}",
                            operationName, maxAttempts, e.getMessage());
                }
            }
        }
        throw new RuntimeException(operationName + " 실패 (최대 " + maxAttempts + "회 재시도)", lastException);
    }
}
