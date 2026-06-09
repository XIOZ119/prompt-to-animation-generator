package org.sieun.prompt2animation.util;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RetryUtilTest {

    @Test
    void execute_successOnFirstAttempt_returnsImmediately() {
        AtomicInteger callCount = new AtomicInteger();

        String result = RetryUtil.execute(3, 0, "test-op", () -> {
            callCount.incrementAndGet();
            return "success";
        });

        assertThat(result).isEqualTo("success");
        assertThat(callCount.get()).isEqualTo(1);
    }

    @Test
    void execute_failsOnceThenSucceeds_returnsResult() {
        AtomicInteger callCount = new AtomicInteger();

        String result = RetryUtil.execute(3, 0, "test-op", () -> {
            if (callCount.incrementAndGet() == 1) {
                throw new RuntimeException("첫 번째 실패");
            }
            return "success";
        });

        assertThat(result).isEqualTo("success");
        assertThat(callCount.get()).isEqualTo(2);
    }

    @Test
    void execute_exhaustsAllAttempts_throwsRuntimeException() {
        assertThatThrownBy(() ->
            RetryUtil.execute(3, 0, "test-op", () -> {
                throw new RuntimeException("항상 실패");
            })
        ).isInstanceOf(RuntimeException.class)
         .hasMessageContaining("test-op");
    }

    @Test
    void execute_exhaustsAllAttempts_wrapsOriginalCause() {
        RuntimeException original = new RuntimeException("원본 예외");

        assertThatThrownBy(() ->
            RetryUtil.execute(3, 0, "test-op", () -> { throw original; })
        ).isInstanceOf(RuntimeException.class)
         .hasCause(original);
    }
}
