package org.sieun.prompt2animation.service;

import lombok.RequiredArgsConstructor;
import org.sieun.prompt2animation.domain.Generation;
import org.sieun.prompt2animation.dto.request.GenerationRequest;
import org.sieun.prompt2animation.dto.response.GenerationResponse;
import org.sieun.prompt2animation.dto.response.GenerationStatusResponse;
import org.sieun.prompt2animation.exception.CustomException;
import org.sieun.prompt2animation.exception.ErrorCode;
import org.sieun.prompt2animation.repository.GenerationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GenerationService {

    private final GenerationRepository generationRepository;

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
}
