package org.sieun.prompt2animation.service;

import lombok.RequiredArgsConstructor;
import org.sieun.prompt2animation.client.openai.OpenAiClient;
import org.sieun.prompt2animation.client.openai.dto.CutGenerationResult;
import org.sieun.prompt2animation.client.openai.dto.SceneGenerationResult;
import org.sieun.prompt2animation.domain.Cut;
import org.sieun.prompt2animation.domain.CutImage;
import org.sieun.prompt2animation.domain.CutVideo;
import org.sieun.prompt2animation.domain.Generation;
import org.sieun.prompt2animation.domain.GenerationStatus;
import org.sieun.prompt2animation.domain.Scene;
import org.sieun.prompt2animation.exception.CustomException;
import org.sieun.prompt2animation.exception.ErrorCode;
import org.sieun.prompt2animation.repository.CutImageRepository;
import org.sieun.prompt2animation.repository.CutRepository;
import org.sieun.prompt2animation.repository.CutVideoRepository;
import org.sieun.prompt2animation.repository.GenerationRepository;
import org.sieun.prompt2animation.repository.SceneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenerationWorkflowProcessor {

    private final GenerationRepository generationRepository;
    private final SceneRepository sceneRepository;
    private final CutRepository cutRepository;
    private final CutImageRepository cutImageRepository;
    private final CutVideoRepository cutVideoRepository;
    private final OpenAiClient openAiClient;

    @Transactional
    public void markProcessing(Long generationId) {
        Generation generation = findGeneration(generationId);
        generation.markProcessing();
    }

    @Transactional
    public void generateScene(Long generationId) {
        Generation generation = findGeneration(generationId);

        SceneGenerationResult result = openAiClient.generateScene(generation.getUserPrompt());

        Scene scene = Scene.create(generation, result.title(), result.scenario());
        sceneRepository.save(scene);

        for (CutGenerationResult cut : result.cuts()) {
            cutRepository.save(Cut.create(scene, cut.cutOrder(), cut.imagePrompt(), cut.videoPrompt(), cut.durationSec()));
        }
    }

    @Transactional
    public void generateCutImages(Long generationId) {
        Generation generation = findGeneration(generationId);
        Scene scene = findScene(generation);
        List<Cut> cuts = cutRepository.findBySceneOrderByCutOrderAsc(scene);

        for (Cut cut : cuts) {
            CutImage cutImage = CutImage.create(cut);
            cutImageRepository.save(cutImage);
            cutImage.markProcessing();
            cutImage.markCompleted(
                    "https://mock.example.com/images/" + generationId + "-cut" + cut.getCutOrder() + ".png");
        }
    }

    @Transactional
    public void generateCutVideos(Long generationId) {
        Generation generation = findGeneration(generationId);
        Scene scene = findScene(generation);
        List<Cut> cuts = cutRepository.findBySceneOrderByCutOrderAsc(scene);

        for (Cut cut : cuts) {
            CutImage cutImage = cutImageRepository
                    .findFirstByCutAndStatusOrderByIdDesc(cut, GenerationStatus.COMPLETED)
                    .orElseThrow(() -> new CustomException(ErrorCode.GENERATION_RESULT_FETCH_FAILED));

            CutVideo cutVideo = CutVideo.create(cut, cutImage);
            cutVideoRepository.save(cutVideo);
            cutVideo.markProcessing();
            cutVideo.markCompleted(
                    "https://mock.example.com/videos/" + generationId + "-cut" + cut.getCutOrder() + ".mp4");
        }
    }

    @Transactional
    public void mergeVideos(Long generationId) {
        Generation generation = findGeneration(generationId);
        generation.markCompleted("https://mock.example.com/results/" + generationId + ".mp4");
    }

    @Transactional
    public void markFailed(Long generationId, String errorMessage) {
        Generation generation = findGeneration(generationId);
        generation.markFailed(errorMessage);
    }

    private Generation findGeneration(Long generationId) {
        return generationRepository.findById(generationId)
                .orElseThrow(() -> new CustomException(ErrorCode.GENERATION_NOT_FOUND));
    }

    private Scene findScene(Generation generation) {
        return sceneRepository.findByGeneration(generation)
                .orElseThrow(() -> new CustomException(ErrorCode.GENERATION_RESULT_FETCH_FAILED));
    }
}
