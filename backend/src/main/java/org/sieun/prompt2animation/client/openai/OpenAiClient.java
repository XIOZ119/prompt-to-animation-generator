package org.sieun.prompt2animation.client.openai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
              "style": "전체 영상에 일관되게 적용할 시각적 스타일 (영문, 예: children's animation style, soft pastel colors, warm lighting)",
              "cuts": [
                {
                  "cutOrder": 1,
                  "imagePrompt": "이미지 생성을 위한 상세 영문 프롬프트 (스타일 지시어 제외, 내용/구도/분위기만 작성)",
                  "videoPrompt": "비디오 생성을 위한 상세 영문 프롬프트 (스타일 지시어 제외, 움직임/전환만 작성)",
                  "durationSec": 5 또는 10 중 하나만 선택 (Kie API 제약)
                }
              ]
            }
            Cut은 4개를 생성하세요. imagePrompt와 videoPrompt는 영문으로 작성하세요.
            style은 전체 영상에 공통으로 적용되므로 각 컷의 imagePrompt와 videoPrompt에는 스타일 지시어를 포함하지 마세요.
            각 cut의 durationSec은 반드시 5 또는 10 중 하나여야 합니다. 총 합이 30초 내외가 되도록 분배하세요.
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
