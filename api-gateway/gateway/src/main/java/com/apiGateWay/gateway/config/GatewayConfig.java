package com.apiGateWay.gateway.config;

import com.apiGateWay.gateway.filter.CartValidationFilter;
import com.apiGateWay.gateway.filter.JwtTokenService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
public class GatewayConfig {

        private final JwtTokenService jwtTokenService;

        public GatewayConfig(JwtTokenService jwtTokenService) {
                this.jwtTokenService = jwtTokenService;
        }

        @Bean
        public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
            return builder.routes()
                    .route("member-login", r -> r.path("/member/login")
                            .filters(f -> f.modifyResponseBody(String.class, String.class, (exchange, responseBody) -> {
                                log.info("=== Modifying login response ===");
                                log.info("Original response: {}", responseBody);
                                if (responseBody == null || responseBody.isEmpty()) {
                                    return Mono.just(responseBody);
                                }
                                try {
                                    ObjectMapper mapper = new ObjectMapper();
                                    JsonNode jsonNode = mapper.readTree(responseBody);
                                    String email = extractEmail(jsonNode);
                                    log.info("Extracted email: {}", email);
                                    if (email != null && !email.isEmpty()) {
                                        log.info("Generating tokens for: {}", email);
                                        String accessToken = jwtTokenService.generateAccessToken(email);
                                        ObjectNode modifiedResponse = (ObjectNode) jsonNode;
                                        modifiedResponse.put("accessToken", accessToken);
                                        modifiedResponse.put("tokenType", "Bearer");
                                        modifiedResponse.put("expiresIn", 3600);
                                        String result = mapper.writeValueAsString(modifiedResponse);
                                        log.info("=== Token added successfully ===");
                                        log.info("Access Token: {}", accessToken);
                                        return Mono.just(result);
                                    }
                                } catch (Exception e) {
                                    log.error("Error modifying response", e);
                                }
                                return Mono.just(responseBody);})).uri("http://localhost:8070"))

                    .route("member-other", r -> r.path("/member/**").uri("http://localhost:8070"))
                    .route("cart-service", r -> r.path("/cart/**").filters(f -> f.filter(new CartValidationFilter(jwtTokenService))).uri("http://localhost:8084"))
                    .route("product-service", r -> r.path("/product/**").uri("http://localhost:8083"))
                    .build();
        }

        private String extractEmail(JsonNode jsonNode) {
            if (jsonNode.has("email")) {
                return jsonNode.get("email").asText();
            }
            if (jsonNode.has("data")) {
                JsonNode dataNode = jsonNode.get("data");
                if (dataNode.has("email")) {
                    return dataNode.get("email").asText();
                }
            }
            if (jsonNode.has("member")) {
                JsonNode memberNode = jsonNode.get("member");
                if (memberNode.has("email")) {
                    return memberNode.get("email").asText();
                }
            }
            return null;
        }
}