package com.training.member.memberassignment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
@Configuration

public class OpenAPIConfig
{
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Member service api")
                        .version("1.0")
                        .description("API documentation for Member service api")
                        .contact(new Contact()
                                .name("")
                                .email("")));
    }
}
