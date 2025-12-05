package com.project.cart.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI configuration
 */
@Configuration
public class SwaggerConfig {

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    @Bean
    public OpenAPI cartServiceOpenAPI() {
        Server server = new Server();
        server.setUrl(contextPath);
        server.setDescription("Cart Service API");

        Contact contact = new Contact();
        contact.setEmail("support@example.com");
        contact.setName("Cart Service Team");
        contact.setUrl("https://www.example.com");

        License license = new License()
                .name("MIT License")
                .url("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
                .title("Shopping Cart API")
                .version("1.0.0")
                .contact(contact)
                .description("RESTful API for managing shopping cart in e-commerce platform")
                .termsOfService("https://www.example.com/terms")
                .license(license);

        // JWT Security Scheme
        SecurityScheme securityScheme = new SecurityScheme()
                .name("Bearer Authentication")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Enter JWT token obtained from /v1/auth/login");

        Components components = new Components()
                .addSecuritySchemes("Bearer Authentication", securityScheme);

        return new OpenAPI()
                .info(info)
                .servers(List.of(server))
                .components(components);
    }
}
