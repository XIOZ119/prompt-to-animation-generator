package org.sieun.prompt2animation.fixture;

import org.sieun.prompt2animation.client.openai.dto.CutGenerationResult;
import org.sieun.prompt2animation.client.openai.dto.SceneGenerationResult;
import org.sieun.prompt2animation.domain.*;
import org.sieun.prompt2animation.dto.request.GenerationRequest;

import java.util.List;
import java.util.stream.IntStream;

public class TestFixtures {

    public static Generation pendingGeneration() {
        return Generation.create("테스트 프롬프트");
    }

    public static Generation processingGeneration() {
        Generation g = Generation.create("테스트 프롬프트");
        g.markProcessing();
        return g;
    }

    public static Generation completedGeneration(String resultUrl) {
        Generation g = Generation.create("테스트 프롬프트");
        g.markProcessing();
        g.markCompleted(resultUrl);
        return g;
    }

    public static Generation failedGeneration() {
        Generation g = Generation.create("테스트 프롬프트");
        g.markProcessing();
        g.markFailed("에러 발생");
        return g;
    }

    public static Generation timeoutGeneration() {
        Generation g = Generation.create("테스트 프롬프트");
        g.markProcessing();
        g.markTimeout();
        return g;
    }

    public static Scene sceneFor(Generation generation) {
        return Scene.create(generation, "테스트 씬 제목", "테스트 씬 시나리오");
    }

    public static Cut cutFor(Scene scene, int cutOrder, int durationSec) {
        return Cut.create(scene, cutOrder, "이미지 프롬프트 " + cutOrder, "비디오 프롬프트 " + cutOrder, durationSec);
    }

    public static CutImage completedCutImage(Cut cut, String imageUrl) {
        CutImage img = CutImage.create(cut);
        img.markProcessing();
        img.markCompleted(imageUrl);
        return img;
    }

    public static CutImage failedCutImage(Cut cut) {
        CutImage img = CutImage.create(cut);
        img.markProcessing();
        img.markFailed("이미지 생성 실패");
        return img;
    }

    public static CutImage processingCutImage(Cut cut) {
        CutImage img = CutImage.create(cut);
        img.markProcessing();
        return img;
    }

    public static CutVideo completedCutVideo(Cut cut, CutImage cutImage, String videoUrl) {
        CutVideo video = CutVideo.create(cut, cutImage);
        video.markProcessing();
        video.markCompleted(videoUrl);
        return video;
    }

    public static CutVideo failedCutVideo(Cut cut) {
        return CutVideo.createFailed(cut, "비디오 생성 실패");
    }

    public static GenerationRequest validRequest() {
        return new GenerationRequest("테스트 프롬프트");
    }

    public static GenerationRequest longPromptRequest() {
        return new GenerationRequest("a".repeat(501));
    }

    public static SceneGenerationResult mockSceneResult(int cutCount) {
        List<CutGenerationResult> cuts = IntStream.rangeClosed(1, cutCount)
                .mapToObj(i -> mockCutResult(i, 5))
                .toList();
        return new SceneGenerationResult("테스트 제목", "테스트 시나리오", "anime style", cuts);
    }

    public static CutGenerationResult mockCutResult(int cutOrder, int durationSec) {
        return new CutGenerationResult(cutOrder, "이미지 프롬프트 " + cutOrder, "비디오 프롬프트 " + cutOrder, durationSec);
    }
}
