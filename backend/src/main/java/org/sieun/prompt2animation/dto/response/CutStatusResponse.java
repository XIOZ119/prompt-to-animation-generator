package org.sieun.prompt2animation.dto.response;

import org.sieun.prompt2animation.domain.GenerationStatus;

public record CutStatusResponse(
        Integer cutOrder,
        GenerationStatus imageStatus,
        GenerationStatus videoStatus
) {
}
