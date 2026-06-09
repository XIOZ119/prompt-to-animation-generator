package org.sieun.prompt2animation.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VideoMergeServiceTest {

    private VideoMergeService videoMergeService;

    @BeforeEach
    void setUp() {
        videoMergeService = new VideoMergeService();
        ReflectionTestUtils.setField(videoMergeService, "uploadDir", "/tmp/test-uploads");
        ReflectionTestUtils.setField(videoMergeService, "baseUrl", "http://localhost");
        ReflectionTestUtils.setField(videoMergeService, "mockMode", true);
    }

    @Test
    void merge_mockMode_returnsConstantMockUrl() {
        String result = videoMergeService.merge(1L, List.of("http://video1.mp4", "http://video2.mp4"));

        assertThat(result).isEqualTo(
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
        );
    }

    @Test
    void merge_mockMode_doesNotAttemptFileSystemOperations() {
        // mock mode에서는 파일시스템 접근 없이 즉시 반환 — 예외 없이 완료되면 통과
        assertThat(videoMergeService.merge(1L, List.of("http://video.mp4"))).isNotNull();
    }
}
