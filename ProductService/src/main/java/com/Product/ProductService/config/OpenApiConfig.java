package com.Product.ProductService.config;

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
                        .title("Product Service")
                        .version("1.0")
                        .description("Contains all products")
                        .contact(new Contact()
                                .name("Amit")
                                .email("amit.pal@gdn-commerce.com")));
    }
}
