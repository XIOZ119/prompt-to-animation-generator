package org.sieun.prompt2animation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sieun.prompt2animation.dto.request.GenerationRequest;
import org.sieun.prompt2animation.dto.response.ApiResponse;
import org.sieun.prompt2animation.dto.response.GenerationResponse;
import org.sieun.prompt2animation.dto.response.GenerationStatusResponse;
import org.sieun.prompt2animation.service.GenerationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/generations")
@RequiredArgsConstructor
public class GenerationController {

    private final GenerationService generationService;

    @PostMapping
    public ApiResponse<GenerationResponse> createGeneration(@Valid @RequestBody GenerationRequest request) {
        return ApiResponse.success("애니메이션 생성 요청 성공", generationService.createGeneration(request));
    }

    @GetMapping("/{generationId}")
    public ApiResponse<GenerationStatusResponse> getGenerationStatus(@PathVariable Long generationId) {
        return ApiResponse.success("생성 상태 조회 성공", generationService.getGenerationStatus(generationId));
    }
}
