package org.sieun.prompt2animation.client.openai.dto;

public record CutGenerationResult(
        Integer cutOrder,
        String imagePrompt,
        String videoPrompt,
        Integer durationSec
) {
}
