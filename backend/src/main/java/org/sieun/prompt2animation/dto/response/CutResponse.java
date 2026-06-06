package org.sieun.prompt2animation.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.sieun.prompt2animation.domain.Cut;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CutResponse {

    private final Integer cutOrder;
    private final Integer durationSec;
    private final String imagePrompt;
    private final String imageUrl;
    private final String videoPrompt;
    private final String videoUrl;

    public static CutResponse of(Cut cut, String imageUrl, String videoUrl) {
        return new CutResponse(
                cut.getCutOrder(),
                cut.getDurationSec(),
                cut.getImagePrompt(),
                imageUrl,
                cut.getVideoPrompt(),
                videoUrl
        );
    }
}
