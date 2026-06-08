package org.sieun.prompt2animation.service;

import lombok.RequiredArgsConstructor;
import org.sieun.prompt2animation.domain.Cut;
import org.sieun.prompt2animation.domain.Generation;
import org.sieun.prompt2animation.domain.GenerationStatus;
import org.sieun.prompt2animation.domain.GenerationStep;
import org.sieun.prompt2animation.domain.Scene;
import org.sieun.prompt2animation.dto.request.GenerationRequest;
import org.sieun.prompt2animation.dto.response.CutResponse;
import org.sieun.prompt2animation.dto.response.CutStatusResponse;
import org.sieun.prompt2animation.dto.response.GenerationResponse;
import org.sieun.prompt2animation.dto.response.GenerationResultResponse;
import org.sieun.prompt2animation.dto.response.GenerationStatusResponse;
import org.sieun.prompt2animation.event.GenerationCreatedEvent;
import org.sieun.prompt2animation.exception.CustomException;
import org.sieun.prompt2animation.exception.ErrorCode;
import org.sieun.prompt2animation.repository.CutImageRepository;
import org.sieun.prompt2animation.repository.CutRepository;
import org.sieun.prompt2animation.repository.CutVideoRepository;
import org.sieun.prompt2animation.repository.GenerationRepository;
import org.sieun.prompt2animation.repository.SceneRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenerationService {

    private final GenerationRepository generationRepository;
    private final SceneRepository sceneRepository;
    private final CutRepository cutRepository;
    private final CutImageRepository cutImageRepository;
    private final CutVideoRepository cutVideoRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public GenerationResponse createGeneration(GenerationRequest request) {
        try {
            Generation generation = Generation.create(request.userPrompt());
            generationRepository.save(generation);
            eventPublisher.publishEvent(new GenerationCreatedEvent(generation.getId()));
            return GenerationResponse.from(generation);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.GENERATION_CREATE_FAILED);
        }
    }

    @Transactional(readOnly = true)
    public GenerationStatusResponse getGenerationStatus(Long generationId) {
        if (generationId == null || generationId <= 0) {
            throw new CustomException(ErrorCode.INVALID_GENERATION_ID);
        }
        try {
            Generation generation = generationRepository.findById(generationId)
                    .orElseThrow(() -> new CustomException(ErrorCode.GENERATION_NOT_FOUND));

            GenerationStatus status = generation.getStatus();

            if (status == GenerationStatus.PENDING) {
                return GenerationStatusResponse.of(generation, null, null, 0, 0, List.of());
            }

            // PENDING 외 모든 상태: 실제 단계 수 계산
            Scene scene = sceneRepository.findByGeneration(generation).orElse(null);

            if (scene == null) {
                return GenerationStatusResponse.of(generation, GenerationStep.SCENE_GENERATION, "Scene 생성 중...", 0, 0, List.of());
            }

            List<Cut> cuts = cutRepository.findBySceneOrderByCutOrderAsc(scene);
            long totalCuts = cuts.size();

            long completedImages = cutImageRepository.countByCut_SceneAndStatus(scene, GenerationStatus.COMPLETED);
            long completedVideos = cutVideoRepository.countByCut_SceneAndStatus(scene, GenerationStatus.COMPLETED);

            int total = (int) (1 + totalCuts + totalCuts + 1);
            int completed = (int) (1 + completedImages + completedVideos + (generation.getResultUrl() != null ? 1 : 0));

            List<CutStatusResponse> cutStatuses = cuts.stream()
                    .map(cut -> new CutStatusResponse(
                            cut.getCutOrder(),
                            cutImageRepository.findFirstByCutOrderByIdDesc(cut).map(img -> img.getStatus()).orElse(null),
                            cutVideoRepository.findFirstByCutOrderByIdDesc(cut).map(vid -> vid.getStatus()).orElse(null)
                    ))
                    .toList();

            if (status == GenerationStatus.COMPLETED) {
                return GenerationStatusResponse.of(generation, GenerationStep.COMPLETED, "완료", total, total, cutStatuses);
            }

            if (status == GenerationStatus.FAILED || status == GenerationStatus.TIMEOUT) {
                return GenerationStatusResponse.of(generation, GenerationStep.FAILED, "실패", completed, total, cutStatuses);
            }

            // PROCESSING: 단계 판단은 processedImages(COMPLETED+FAILED) 기준
            List<GenerationStatus> processedStatuses = List.of(GenerationStatus.COMPLETED, GenerationStatus.FAILED);
            long processedImages = cutImageRepository.countByCut_SceneAndStatusIn(scene, processedStatuses);
            long processedVideos = cutVideoRepository.countByCut_SceneAndStatusIn(scene, processedStatuses);

            GenerationStep currentStep;
            String currentStepMessage;

            if (processedImages < totalCuts) {
                currentStep = GenerationStep.CUT_IMAGE_GENERATION;
                currentStepMessage = "컷 " + (processedImages + 1) + " 이미지 생성 중...";
            } else if (processedVideos < totalCuts) {
                currentStep = GenerationStep.CUT_VIDEO_GENERATION;
                currentStepMessage = "컷 " + (processedVideos + 1) + " 비디오 생성 중...";
            } else {
                currentStep = GenerationStep.VIDEO_MERGE;
                currentStepMessage = "최종 영상 병합 중...";
            }

            return GenerationStatusResponse.of(generation, currentStep, currentStepMessage, completed, total, cutStatuses);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.GENERATION_STATUS_FETCH_FAILED);
        }
    }

    @Transactional(readOnly = true)
    public GenerationResultResponse getGenerationResult(Long generationId) {
        if (generationId == null || generationId <= 0) {
            throw new CustomException(ErrorCode.INVALID_GENERATION_ID);
        }
        try {
            Generation generation = generationRepository.findById(generationId)
                    .orElseThrow(() -> new CustomException(ErrorCode.GENERATION_NOT_FOUND));

            GenerationStatus status = generation.getStatus();
            if (status != GenerationStatus.COMPLETED && status != GenerationStatus.FAILED) {
                throw new CustomException(ErrorCode.GENERATION_NOT_COMPLETED);
            }

            Scene scene = sceneRepository.findByGeneration(generation).orElse(null);
            if (scene == null) {
                return GenerationResultResponse.of(generation, null, List.of());
            }

            List<Cut> cuts = cutRepository.findBySceneOrderByCutOrderAsc(scene);

            List<CutResponse> cutResponses = cuts.stream()
                    .map(cut -> {
                        String imageUrl = cutImageRepository
                                .findFirstByCutAndStatusOrderByIdDesc(cut, GenerationStatus.COMPLETED)
                                .map(img -> img.getImageUrl())
                                .orElse(null);
                        String videoUrl = cutVideoRepository
                                .findFirstByCutAndStatusOrderByIdDesc(cut, GenerationStatus.COMPLETED)
                                .map(vid -> vid.getVideoUrl())
                                .orElse(null);
                        return CutResponse.of(cut, imageUrl, videoUrl);
                    })
                    .toList();

            return GenerationResultResponse.of(generation, scene, cutResponses);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.GENERATION_RESULT_FETCH_FAILED);
        }
    }
}
