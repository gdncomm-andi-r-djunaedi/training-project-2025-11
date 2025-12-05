package com.gdn.training.product_service.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI productServiceOpenAPI(){
        return new OpenAPI()
                .info(new Info()
                        .title("Product Service API")
                        .description("API for product listing, search and get details")
                        .version("1.0"));
    }
}
