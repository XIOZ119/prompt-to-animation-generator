package org.sieun.prompt2animation.dto.response;

import org.sieun.prompt2animation.domain.Generation;

import java.time.LocalDateTime;

public record GenerationHistoryResponse(
        Long generationId,
        String title,
        String thumbnailUrl,
        String status,
        Integer durationSec,
        LocalDateTime createdAt,
        String errorMessage
) {
    public static GenerationHistoryResponse of(Generation generation, String title, String thumbnailUrl, Integer durationSec) {
        return new GenerationHistoryResponse(
                generation.getId(),
                title,
                thumbnailUrl,
                generation.getStatus().name(),
                durationSec,
                generation.getCreatedAt(),
                generation.getErrorMessage()
        );
    }
}
