package org.sieun.prompt2animation.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.sieun.prompt2animation.domain.Generation;
import org.sieun.prompt2animation.domain.GenerationStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GenerationStatusResponse {

    private final Long generationId;
    private final GenerationStatus status;
    private final Integer progress;
    private final String currentStep;
    private final String currentStepMessage;
    private final Integer completedStepCount;
    private final Integer totalStepCount;
    private final String errorMessage;

    public static GenerationStatusResponse from(Generation generation) {
        GenerationStatus status = generation.getStatus();

        Integer progress = switch (status) {
            case PENDING -> 0;
            case COMPLETED -> 100;
            default -> null;
        };

        String currentStep = switch (status) {
            case COMPLETED -> "COMPLETED";
            case FAILED, TIMEOUT -> "FAILED";
            default -> null;
        };

        String currentStepMessage = switch (status) {
            case COMPLETED -> "완료";
            case FAILED, TIMEOUT -> "실패";
            default -> null;
        };

        return new GenerationStatusResponse(
                generation.getId(),
                status,
                progress,
                currentStep,
                currentStepMessage,
                null,
                null,
                generation.getErrorMessage()
        );
    }
}
