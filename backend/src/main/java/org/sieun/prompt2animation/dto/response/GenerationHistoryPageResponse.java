package org.sieun.prompt2animation.dto.response;

import org.sieun.prompt2animation.domain.Generation;
import org.springframework.data.domain.Page;

import java.util.List;

public record GenerationHistoryPageResponse(
        List<GenerationHistoryResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
    public static GenerationHistoryPageResponse of(Page<Generation> generationPage, List<GenerationHistoryResponse> content) {
        return new GenerationHistoryPageResponse(
                content,
                generationPage.getNumber(),
                generationPage.getSize(),
                generationPage.getTotalElements(),
                generationPage.getTotalPages(),
                generationPage.hasNext(),
                generationPage.hasPrevious()
        );
    }
}
