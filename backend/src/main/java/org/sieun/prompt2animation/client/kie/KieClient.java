package org.sieun.prompt2animation.client.kie;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.sieun.prompt2animation.client.kie.dto.ImageCreateRequest;
import org.sieun.prompt2animation.client.kie.dto.ImageCreateRequest.Input;
import org.sieun.prompt2animation.client.kie.dto.RecordInfoResponse;
import org.sieun.prompt2animation.client.kie.dto.TaskCreateResponse;
import org.sieun.prompt2animation.client.kie.dto.VideoCreateRequest;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class KieClient {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int MAX_ATTEMPTS = 60;
    private static final long POLL_INTERVAL_MS = 3_000L;

    private final RestClient kieRestClient;

    @Value("${kie.model}")
    private String model;

    @Value("${kie.video-model}")
    private String videoModel;

    public String generateImage(String imagePrompt) {
        try {
            String taskId = createTask(imagePrompt);
            return pollForResult(taskId);
        } catch (Exception e) {
            throw new RuntimeException("Kie 이미지 생성 실패: " + e.getMessage(), e);
        }
    }

    public String generateVideo(String videoPrompt, String imageUrl, Integer durationSec) {
        try {
            String taskId = createVideoTask(videoPrompt, imageUrl, durationSec);
            return pollForResult(taskId);
        } catch (Exception e) {
            throw new RuntimeException("Kie 비디오 생성 실패: " + e.getMessage(), e);
        }
    }

    private String createVideoTask(String videoPrompt, String imageUrl, Integer durationSec) {
        VideoCreateRequest request = new VideoCreateRequest(
                videoModel,
                new VideoCreateRequest.Input(videoPrompt, List.of(imageUrl), false, String.valueOf(durationSec))
        );

        TaskCreateResponse response = kieRestClient.post()
                .uri("/jobs/createTask")
                .body(request)
                .retrieve()
                .body(TaskCreateResponse.class);

        return response.data().taskId();
    }

    private String createTask(String imagePrompt) {
        ImageCreateRequest request = new ImageCreateRequest(model, Input.of(imagePrompt));

        TaskCreateResponse response = kieRestClient.post()
                .uri("/jobs/createTask")
                .body(request)
                .retrieve()
                .body(TaskCreateResponse.class);

        return response.data().taskId();
    }

    private String pollForResult(String taskId) throws Exception {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            Thread.sleep(POLL_INTERVAL_MS);

            RecordInfoResponse response = kieRestClient.get()
                    .uri("/jobs/recordInfo?taskId={taskId}", taskId)
                    .retrieve()
                    .body(RecordInfoResponse.class);

            validateResponseCode(response.code(), response.msg());

            String state = response.data().state();

            if ("success".equalsIgnoreCase(state)) {
                return extractImageUrl(response.data().resultJson());
            }

            if ("fail".equalsIgnoreCase(state)) {
                throw new RuntimeException("Kie 이미지 생성 실패: " + response.data().failMsg());
            }
        }

        throw new RuntimeException("Kie 이미지 생성 타임아웃: taskId=" + taskId);
    }

    private void validateResponseCode(Integer code, String msg) {
        if (code == null || code == 200) return;
        throw new RuntimeException("Kie API 오류 (code=" + code + "): " + msg);
    }

    private String extractImageUrl(String resultJson) throws Exception {
        JsonNode node = OBJECT_MAPPER.readTree(resultJson);
        return node.get("resultUrls").get(0).asText();
    }
}
