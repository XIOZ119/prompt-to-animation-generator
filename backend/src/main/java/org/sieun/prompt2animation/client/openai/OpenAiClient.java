package org.sieun.prompt2animation.client.openai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.sieun.prompt2animation.client.openai.dto.ChatRequest;
import org.sieun.prompt2animation.client.openai.dto.ChatRequest.Message;
import org.sieun.prompt2animation.client.openai.dto.ChatRequest.ResponseFormat;
import org.sieun.prompt2animation.client.openai.dto.ChatResponse;
import org.sieun.prompt2animation.client.openai.dto.SceneGenerationResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiClient {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String SCENE_SYSTEM_PROMPT = """
            당신은 애니메이션 스토리보드 작가입니다.
            사용자의 프롬프트를 기반으로 애니메이션 장면을 설계하고 다음 JSON 형식으로만 응답하세요.
            {
              "title": "장면 제목",
              "scenario": "전체 시나리오 설명",
              "cuts": [
                {
                  "cutOrder": 1,
                  "imagePrompt": "이미지 생성을 위한 상세 영문 프롬프트",
                  "videoPrompt": "비디오 생성을 위한 상세 영문 프롬프트",
                  "durationSec": 장면의 내용과 전체 영상 길이를 고려하여 적절한 초 단위 정수
                }
              ]
            }
            Cut은 4개를 생성하세요. imagePrompt와 videoPrompt는 영문으로 작성하세요.
            각 cut의 durationSec은 장면의 흐름과 전체 영상 길이에 맞게 자유롭게 결정하세요.
            각 cut의 durationSec 총 합은 30초 내외로 설정하세요.
            """;

    private final RestClient openAiRestClient;

    @Value("${openai.model}")
    private String model;

    public SceneGenerationResult generateScene(String userPrompt) {
        try {
            ChatRequest request = new ChatRequest(
                    model,
                    List.of(
                            new Message("system", SCENE_SYSTEM_PROMPT),
                            new Message("user", userPrompt)
                    ),
                    ResponseFormat.jsonObject()
            );

            ChatResponse response = openAiRestClient.post()
                    .uri("/chat/completions")
                    .body(request)
                    .retrieve()
                    .body(ChatResponse.class);

            String content = response.choices().get(0).message().content();
            return OBJECT_MAPPER.readValue(content, SceneGenerationResult.class);
        } catch (Exception e) {
            log.error("[OpenAI] Scene 생성 실패 - prompt: {}, error: {}", userPrompt, e.getMessage(), e);
            throw new RuntimeException("OpenAI Scene 생성 실패: " + e.getMessage(), e);
        }
    }
}
