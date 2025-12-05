package com.blibli.SearchService.config;

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
                        .title("Search Service")
                        .version("1.0")
                        .description("Api Documentation for Search Service")
                        .contact(new Contact()
                                .name("Pranav")
                                .email("pranav.garg@gdn-commerce.com")));
    }
}


