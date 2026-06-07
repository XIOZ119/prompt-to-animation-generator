package org.sieun.prompt2animation.client.kie.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ImageCreateRequest(
        String model,
        Input input
) {
    public record Input(
            String prompt,
            @JsonProperty("output_format") String outputFormat,
            @JsonProperty("aspect_ratio") String aspectRatio
    ) {
        public static Input of(String prompt) {
            return new Input(prompt, "png", "16:9");
        }
    }
}
