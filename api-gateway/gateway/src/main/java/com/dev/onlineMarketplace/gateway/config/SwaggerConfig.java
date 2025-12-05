package com.dev.onlineMarketplace.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.port:8070}")
    private String serverPort;

    /**
     * Customizes the Gateway's own OpenAPI docs
     * External services (Member, Product, Cart) are configured via application.properties
     * using springdoc.swagger-ui.urls to aggregate their API docs
     */
    @Bean
    public OpenAPI gatewayOpenAPI() {
        Server server = new Server();
        server.setUrl("http://localhost:" + serverPort);
        server.setDescription("Gateway Server");

        return new OpenAPI()
                .servers(List.of(server))
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("Gateway API")
                        .description("API Gateway for Online Marketplace - Aggregated Services")
                        .version("v1.0")
                        .contact(new io.swagger.v3.oas.models.info.Contact()
                                .name("Gateway Service")
                                .email("support@example.com"))
                        .license(new io.swagger.v3.oas.models.info.License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}

