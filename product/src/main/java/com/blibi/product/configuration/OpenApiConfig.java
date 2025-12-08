package com.blibi.product.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for OpenAPI/Swagger documentation.
 * 
 * This class configures the OpenAPI specification for the Product Service,
 * which generates interactive API documentation accessible via Swagger UI.
 * The documentation is automatically generated from the controller annotations
 * and this configuration.
 * 
 * Access points:
 * - Swagger UI: http://localhost:8080/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8080/v3/api-docs
 * 
 * @Configuration - Marks this class as a Spring configuration class, making it
 *                 eligible for component scanning
 * 
 * @author Product Service Team
 */
@Configuration
public class OpenApiConfig {
    
    /**
     * Creates and configures the OpenAPI bean for API documentation.
     * 
     * This bean defines the metadata for the API documentation, including:
     * - API title
     * - API description
     * - API version
     * 
     * SpringDoc OpenAPI automatically scans the application for @RestController
     * and @RequestMapping annotations to generate the complete API documentation.
     * 
     * @Bean - Registers this method's return value as a Spring bean in the
     *        application context
     * @return OpenAPI object containing API metadata and configuration
     */
    @Bean
    public OpenAPI usersMicroserviceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Product")
                        .description("Product Service")
                        .version("1.0"));
    }
}
