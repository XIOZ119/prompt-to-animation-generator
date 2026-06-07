package org.sieun.prompt2animation.dto.response;

import org.sieun.prompt2animation.domain.Scene;

public record SceneResponse(
        String title,
        String scenario
) {
    public static SceneResponse from(Scene scene) {
        return new SceneResponse(scene.getTitle(), scene.getScenario());
    }
}
