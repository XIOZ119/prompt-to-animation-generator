package org.sieun.prompt2animation.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.sieun.prompt2animation.domain.Generation;
import org.sieun.prompt2animation.domain.GenerationStatus;
import org.sieun.prompt2animation.domain.GenerationStep;

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

    public static GenerationStatusResponse of(
            Generation generation,
            GenerationStep currentStep,
            String currentStepMessage,
            int completedStepCount,
            int totalStepCount
    ) {
        Integer progress = totalStepCount > 0
                ? completedStepCount * 100 / totalStepCount
                : 0;

        return new GenerationStatusResponse(
                generation.getId(),
                generation.getStatus(),
                progress,
                currentStep != null ? currentStep.name() : null,
                currentStepMessage,
                completedStepCount,
                totalStepCount,
                generation.getErrorMessage()
        );
    }
}
