package org.sieun.prompt2animation.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.sieun.prompt2animation.domain.Scene;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SceneResponse {

    private final String title;
    private final String scenario;

    public static SceneResponse from(Scene scene) {
        return new SceneResponse(scene.getTitle(), scene.getScenario());
    }
}
