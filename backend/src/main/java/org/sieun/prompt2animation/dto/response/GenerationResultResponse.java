package org.sieun.prompt2animation.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.sieun.prompt2animation.domain.Generation;
import org.sieun.prompt2animation.domain.Scene;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GenerationResultResponse {

    private final Long generationId;
    private final String resultUrl;
    private final SceneResponse scene;
    private final List<CutResponse> cuts;

    public static GenerationResultResponse of(Generation generation, Scene scene, List<CutResponse> cuts) {
        return new GenerationResultResponse(
                generation.getId(),
                generation.getResultUrl(),
                SceneResponse.from(scene),
                cuts
        );
    }
}
