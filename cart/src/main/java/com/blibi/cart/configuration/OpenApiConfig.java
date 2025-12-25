package com.blibi.cart.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for OpenAPI/Swagger documentation.
 * 
 * This class configures the OpenAPI specification for the Cart Service,
 * which enables interactive API documentation via Swagger UI.
 * 
 * Key Features:
 * - Defines API metadata (title, description, version)
 * - Enables Swagger UI at /swagger-ui.html
 * - Provides API documentation at /v3/api-docs
 * 
 * @Configuration: Marks this class as a Spring configuration class
 * @Bean: Registers OpenAPI bean in Spring context
 * 
 *        Access Points:
 *        - Swagger UI: http://localhost:8081/swagger-ui.html
 *        - API Docs JSON: http://localhost:8081/v3/api-docs
 *
 */
@Configuration
public class OpenApiConfig {

    /**
     * Creates and configures the OpenAPI bean for API documentation.
     * 
     * This bean is used by SpringDoc OpenAPI to generate Swagger documentation
     * for all REST endpoints in the application.
     * 
     * Configuration includes:
     * - API title: "Cart"
     * - API description: "Cart Service"
     * - API version: "1.0"
     * 
     * @return Configured OpenAPI object for Swagger documentation
     */
    @Bean
    public OpenAPI usersMicroserviceOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Cart")
                        .description("Cart Service")
                        .version("1.0"));
    }
}
