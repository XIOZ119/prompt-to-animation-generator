package org.sieun.prompt2animation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GenerationRequest(
        @NotBlank(message = "PROMPT_REQUIRED")
        @Size(max = 500, message = "PROMPT_TOO_LONG")
        String userPrompt
) {
}
