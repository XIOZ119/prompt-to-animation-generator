package org.sieun.prompt2animation.dto.response;

import org.sieun.prompt2animation.domain.Generation;
import org.sieun.prompt2animation.domain.GenerationStatus;
import org.sieun.prompt2animation.domain.GenerationStep;

import java.util.List;

public record GenerationStatusResponse(
        Long generationId,
        GenerationStatus status,
        Integer progress,
        String currentStep,
        String currentStepMessage,
        Integer completedStepCount,
        Integer totalStepCount,
        String errorMessage,
        List<CutStatusResponse> cuts
) {
    public static GenerationStatusResponse of(
            Generation generation,
            GenerationStep currentStep,
            String currentStepMessage,
            int completedStepCount,
            int totalStepCount,
            List<CutStatusResponse> cuts
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
                generation.getErrorMessage(),
                cuts
        );
    }
}
