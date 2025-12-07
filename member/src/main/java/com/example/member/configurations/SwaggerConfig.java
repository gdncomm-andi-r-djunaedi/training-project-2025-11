package com.example.member.configurations;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;

public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Member API")
                        .version("1.0")
                        .description("API documentation for Member application")
                        .contact(new Contact()
                                .name("")
                                .email("")));
    }
}
