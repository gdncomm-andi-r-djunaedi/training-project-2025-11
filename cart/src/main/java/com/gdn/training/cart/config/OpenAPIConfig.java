package com.gdn.training.cart.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI cartServiceAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cart Service API")
                        .description("API for shopping cart management")
                        .version("1.0.0"))
                .components(new Components()
                        .addSecuritySchemes("X-User-Id", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-User-Id")))
                .addSecurityItem(new SecurityRequirement().addList("X-User-Id"));
    }
}
