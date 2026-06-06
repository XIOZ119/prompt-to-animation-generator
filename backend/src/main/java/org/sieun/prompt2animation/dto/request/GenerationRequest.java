package org.sieun.prompt2animation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GenerationRequest {

    @NotBlank(message = "PROMPT_REQUIRED")
    @Size(max = 500, message = "PROMPT_TOO_LONG")
    private String userPrompt;
}
