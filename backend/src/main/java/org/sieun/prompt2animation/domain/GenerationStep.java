package org.sieun.prompt2animation.domain;

public enum GenerationStep {
    SCENE_GENERATION,
    CUT_IMAGE_GENERATION,
    CUT_VIDEO_GENERATION,
    VIDEO_MERGE,
    COMPLETED,
    FAILED
}
