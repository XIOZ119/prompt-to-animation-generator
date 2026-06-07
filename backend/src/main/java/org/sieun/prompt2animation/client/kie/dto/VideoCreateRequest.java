package org.sieun.prompt2animation.client.kie.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record VideoCreateRequest(
        String model,
        Input input
) {
    public record Input(
            String prompt,
            @JsonProperty("image_urls") List<String> imageUrls,
            boolean sound,
            String duration
    ) {}
}
