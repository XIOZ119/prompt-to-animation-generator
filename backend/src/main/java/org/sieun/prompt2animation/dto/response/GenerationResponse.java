package org.sieun.prompt2animation.dto.response;

import org.sieun.prompt2animation.domain.Generation;
import org.sieun.prompt2animation.domain.GenerationStatus;

import java.time.LocalDateTime;

public record GenerationResponse(
        Long generationId,
        GenerationStatus status,
        LocalDateTime createdAt
) {
    public static GenerationResponse from(Generation generation) {
        return new GenerationResponse(
                generation.getId(),
                generation.getStatus(),
                generation.getCreatedAt()
        );
    }
}
