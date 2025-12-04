package com.example.member.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Open Marketplace - Member Service")
                        .version("1.0")
                        .description("Product Service for the Open Marketplace.")
                        .contact(new Contact()
                                .name("")
                                .email("")));
    }
}
