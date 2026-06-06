package org.sieun.prompt2animation.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Prompt-to-Animation API")
                        .version("v1.0.0")
                        .description("사용자 프롬프트를 기반으로 애니메이션을 생성하는 서비스 API"));
    }
}
