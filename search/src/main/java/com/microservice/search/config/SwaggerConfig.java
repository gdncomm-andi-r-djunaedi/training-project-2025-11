package com.microservice.search.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Search Service API")
                        .description("REST API for Product Search Service using Spring Boot and Elasticsearch")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Search Service")
                                .email("")));
    }
}