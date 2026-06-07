package org.sieun.prompt2animation.domain;

public enum GenerationStep {
    SCENE_GENERATION("Scene 생성"),
    CUT_IMAGE_GENERATION("컷 이미지 생성"),
    CUT_VIDEO_GENERATION("컷 비디오 생성"),
    VIDEO_MERGE("영상 병합"),
    COMPLETED("완료"),
    FAILED("실패");

    private final String displayName;

    GenerationStep(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
