package org.sieun.prompt2animation.dto.response;

import org.sieun.prompt2animation.domain.Generation;
import org.sieun.prompt2animation.domain.Scene;

import java.util.List;

public record GenerationResultResponse(
        Long generationId,
        String resultUrl,
        SceneResponse scene,
        List<CutResponse> cuts
) {
    public static GenerationResultResponse of(Generation generation, Scene scene, List<CutResponse> cuts) {
        return new GenerationResultResponse(
                generation.getId(),
                generation.getResultUrl(),
                SceneResponse.from(scene),
                cuts
        );
    }
}
