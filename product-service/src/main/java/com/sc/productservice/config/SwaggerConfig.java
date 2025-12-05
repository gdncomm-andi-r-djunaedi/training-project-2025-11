package com.sc.productservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        System.out.println("SwaggerConfig loaded");
        return new OpenAPI()
                .info(new Info()
                        .title("Market place Application")
                        .version("1.0")
                        .description("API documentation for Ecommerce Marketplace"));
    }
}