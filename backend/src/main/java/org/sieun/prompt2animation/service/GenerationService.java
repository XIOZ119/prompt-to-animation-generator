package org.sieun.prompt2animation.service;

import lombok.RequiredArgsConstructor;
import org.sieun.prompt2animation.domain.Cut;
import org.sieun.prompt2animation.domain.Generation;
import org.sieun.prompt2animation.domain.GenerationStatus;
import org.sieun.prompt2animation.domain.Scene;
import org.sieun.prompt2animation.dto.request.GenerationRequest;
import org.sieun.prompt2animation.dto.response.CutResponse;
import org.sieun.prompt2animation.dto.response.GenerationResponse;
import org.sieun.prompt2animation.dto.response.GenerationResultResponse;
import org.sieun.prompt2animation.dto.response.GenerationStatusResponse;
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
public class GenerationService {

    private final GenerationRepository generationRepository;
    private final SceneRepository sceneRepository;
    private final CutRepository cutRepository;
    private final CutImageRepository cutImageRepository;
    private final CutVideoRepository cutVideoRepository;

    @Transactional
    public GenerationResponse createGeneration(GenerationRequest request) {
        try {
            Generation generation = Generation.create(request.getUserPrompt());
            generationRepository.save(generation);
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
            return GenerationStatusResponse.from(generation);
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

            if (generation.getStatus() != GenerationStatus.COMPLETED) {
                throw new CustomException(ErrorCode.GENERATION_NOT_COMPLETED);
            }

            Scene scene = sceneRepository.findByGeneration(generation)
                    .orElseThrow(() -> new CustomException(ErrorCode.GENERATION_RESULT_FETCH_FAILED));

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
