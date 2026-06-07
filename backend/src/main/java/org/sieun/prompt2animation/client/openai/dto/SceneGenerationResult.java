package org.sieun.prompt2animation.client.openai.dto;

import java.util.List;

public record SceneGenerationResult(
        String title,
        String scenario,
        List<CutGenerationResult> cuts
) {
}
