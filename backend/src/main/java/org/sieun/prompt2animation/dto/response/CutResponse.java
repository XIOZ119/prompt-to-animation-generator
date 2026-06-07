package org.sieun.prompt2animation.dto.response;

import org.sieun.prompt2animation.domain.Cut;

public record CutResponse(
        Integer cutOrder,
        Integer durationSec,
        String imagePrompt,
        String imageUrl,
        String videoPrompt,
        String videoUrl
) {
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
