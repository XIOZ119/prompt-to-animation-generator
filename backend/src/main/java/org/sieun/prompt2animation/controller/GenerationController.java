package org.sieun.prompt2animation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sieun.prompt2animation.dto.request.GenerationRequest;
import org.sieun.prompt2animation.dto.response.ApiResponse;
import org.sieun.prompt2animation.dto.response.GenerationHistoryPageResponse;
import org.sieun.prompt2animation.dto.response.GenerationResponse;
import org.sieun.prompt2animation.dto.response.GenerationResultResponse;
import org.sieun.prompt2animation.dto.response.GenerationStatusResponse;
import org.sieun.prompt2animation.service.GenerationService;
import org.sieun.prompt2animation.service.GenerationWorkflowService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Generation", description = "애니메이션 생성 API")
@RestController
@RequestMapping("/api/generations")
@RequiredArgsConstructor
public class GenerationController {

    private final GenerationService generationService;
    private final GenerationWorkflowService generationWorkflowService;

    @Operation(summary = "생성 기록 조회")
    @GetMapping
    public ApiResponse<GenerationHistoryPageResponse> getGenerationHistory(
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(defaultValue = "latest") String sort
    ) {
        return ApiResponse.success("생성 기록 조회 성공", generationService.getGenerationHistory(status, page, size, sort));
    }

    @Operation(summary = "애니메이션 생성 요청")
    @PostMapping
    public ApiResponse<GenerationResponse> createGeneration(@Valid @RequestBody GenerationRequest request) {
        return ApiResponse.success("애니메이션 생성 요청 성공", generationService.createGeneration(request));
    }

    @Operation(summary = "생성 상태 조회")
    @GetMapping("/{generationId}")
    public ApiResponse<GenerationStatusResponse> getGenerationStatus(@PathVariable Long generationId) {
        return ApiResponse.success("생성 상태 조회 성공", generationService.getGenerationStatus(generationId));
    }

    @Operation(summary = "생성 결과 조회")
    @GetMapping("/{generationId}/result")
    public ApiResponse<GenerationResultResponse> getGenerationResult(@PathVariable Long generationId) {
        return ApiResponse.success("생성 결과 조회 성공", generationService.getGenerationResult(generationId));
    }

    @Operation(summary = "[테스트용] 기존 이미지로 영상 생성 + 병합 재실행")
    @PostMapping("/{generationId}/retry-videos")
    public ApiResponse<Void> retryVideos(@PathVariable Long generationId) {
        generationWorkflowService.retryFromVideo(generationId);
        return ApiResponse.success("영상 생성 재시도 요청 성공", null);
    }
}
