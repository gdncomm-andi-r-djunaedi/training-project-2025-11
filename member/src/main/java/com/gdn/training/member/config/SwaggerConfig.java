package com.gdn.training.member.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI memberOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Member Service API")
                        .description("API for managing members")
                        .version("v1.0.0"));
    }
}
