package com.dev.onlineMarketplace.gateway.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Controller to transform service OpenAPI specs to use Gateway URLs
 * Hidden from Swagger UI - these are internal endpoints for API doc aggregation
 */
@Hidden
@RestController
public class OpenApiTransformController {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${server.port:8070}")
    private String gatewayPort;

    public OpenApiTransformController(ObjectMapper objectMapper) {
        this.webClient = WebClient.builder().build();
        this.objectMapper = objectMapper;
    }

    /**
     * Get Member Service API docs with Gateway URL
     */
    @GetMapping("/api/v1/member/v3/api-docs")
    public Mono<String> getMemberServiceDocs() {
        return transformApiDocs(
            "http://localhost:8061/v3/api-docs",
            "http://localhost:" + gatewayPort,
            "" // No base path - paths in spec already have /api/v1/member
        );
    }

    /**
     * Get Product Service API docs with Gateway URL
     */
    @GetMapping("/api/v1/products/api-docs")
    public Mono<String> getProductServiceDocs() {
        return transformApiDocs(
            "http://localhost:8062/api-docs",
            "http://localhost:" + gatewayPort,
            "" // No base path - paths in spec already have /api/v1/products
        );
    }

    /**
     * Get Cart Service API docs with Gateway URL
     */
    @GetMapping("/api/v1/cart/api-docs")
    public Mono<String> getCartServiceDocs() {
        return transformApiDocs(
            "http://localhost:8063/api-docs",
            "http://localhost:" + gatewayPort,
            "" // No base path - paths in spec already have /api/v1/cart
        );
    }

    /**
     * Transform API docs to use Gateway URL
     */
    private Mono<String> transformApiDocs(String serviceUrl, String gatewayUrl, String basePath) {
        return webClient.get()
                .uri(serviceUrl)
                .retrieve()
                .bodyToMono(String.class)
                .map(json -> {
                    try {
                        JsonNode root = objectMapper.readTree(json);
                        ObjectNode objectNode = (ObjectNode) root;

                        // Create new servers array with Gateway URL
                        ArrayNode servers = objectMapper.createArrayNode();
                        ObjectNode server = objectMapper.createObjectNode();

                        // Set server URL (with or without base path)
                        String serverUrl = basePath != null && !basePath.isEmpty()
                            ? gatewayUrl + basePath
                            : gatewayUrl;

                        server.put("url", serverUrl);
                        server.put("description", "API Gateway");
                        servers.add(server);

                        // Replace servers in the document
                        objectNode.set("servers", servers);

                        return objectMapper.writeValueAsString(objectNode);
                    } catch (Exception e) {
                        return json; // Return original if transformation fails
                    }
                });
    }
}

