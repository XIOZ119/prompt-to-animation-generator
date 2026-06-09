package org.sieun.prompt2animation.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GenerationTest {

    @Test
    void create_initializesPendingStatus() {
        Generation generation = Generation.create("테스트 프롬프트");

        assertThat(generation.getStatus()).isEqualTo(GenerationStatus.PENDING);
        assertThat(generation.getUserPrompt()).isEqualTo("테스트 프롬프트");
        assertThat(generation.getResultUrl()).isNull();
        assertThat(generation.getErrorMessage()).isNull();
        assertThat(generation.getCompletedAt()).isNull();
    }

    @Test
    void markProcessing_changesStatus() {
        Generation generation = Generation.create("테스트");
        generation.markProcessing();

        assertThat(generation.getStatus()).isEqualTo(GenerationStatus.PROCESSING);
    }

    @Test
    void markCompleted_setsResultUrlAndCompletedAt() {
        Generation generation = Generation.create("테스트");
        generation.markCompleted("http://result.mp4");

        assertThat(generation.getStatus()).isEqualTo(GenerationStatus.COMPLETED);
        assertThat(generation.getResultUrl()).isEqualTo("http://result.mp4");
        assertThat(generation.getCompletedAt()).isNotNull();
    }

    @Test
    void markFailed_setsErrorMessageAndCompletedAt() {
        Generation generation = Generation.create("테스트");
        generation.markFailed("에러 메시지");

        assertThat(generation.getStatus()).isEqualTo(GenerationStatus.FAILED);
        assertThat(generation.getErrorMessage()).isEqualTo("에러 메시지");
        assertThat(generation.getCompletedAt()).isNotNull();
    }

    @Test
    void markTimeout_changesStatusToTimeout() {
        Generation generation = Generation.create("테스트");
        generation.markTimeout();

        assertThat(generation.getStatus()).isEqualTo(GenerationStatus.TIMEOUT);
        assertThat(generation.getCompletedAt()).isNotNull();
    }
}
