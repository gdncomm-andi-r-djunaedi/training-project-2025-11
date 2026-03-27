package com.blibi.blibligatway.config;

import com.blibi.blibligatway.filter.AuthGatewayFilterFactory;
import com.blibi.blibligatway.security.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class GatewayConfig {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthGatewayFilterFactory authFilter;

    @Value("${jwt.expiration:3600000}")
    private Long expiration;

    @Value("${services.product.url:http://localhost:8082}")
    private String productServiceUrl;

    @Value("${services.search.url:http://localhost:8083}")
    private String searchServiceUrl;

    @Value("${services.cart.url:http://localhost:8084}")
    private String cartServiceUrl;


    private Mono<Void> handleLogout(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpResponse response = exchange.getResponse();

        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .maxAge(Duration.ofSeconds(0))
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Strict")
                .build();

        response.addCookie(cookie);
        response.getHeaders().add("Logout-Instruction", "Client must discard local Authorization: Bearer token.");
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().add("Content-Type", "application/json");

        try {
            ObjectNode jsonResponse = objectMapper.createObjectNode();
            jsonResponse.put("success", true);
            jsonResponse.put("message", "Logout successful");

            return response.writeWith(Mono.just(response.bufferFactory().wrap(
                    objectMapper.writeValueAsBytes(jsonResponse))));
        } catch (Exception e) {
            return response.writeWith(Mono.just(response.bufferFactory().wrap(
                    "{\"success\":true,\"message\":\"Logout successful\"}".getBytes())));
        }
    }

    private Mono<String> modifyLoginResponse(ServerWebExchange exchange, String body) {
        ServerHttpResponse response = exchange.getResponse();

        if (body == null || body.isEmpty()) {
            return Mono.just("{\"success\":false,\"message\":\"Empty response from member service\"}");
        }

        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                JsonNode jsonNode = objectMapper.readTree(body);

                if (jsonNode.has("success") && jsonNode.get("success").asBoolean()
                        && jsonNode.has("data") && !jsonNode.get("data").isNull()) {
                    JsonNode dataNode = jsonNode.get("data");

                    String userId = dataNode.has("id") ? dataNode.get("id").asText() : "unknown";
                    String email = dataNode.has("email") ? dataNode.get("email").asText() : "unknown@example.com";

                    List<String> roles = new ArrayList<>();
                    if (dataNode.has("roles") && dataNode.get("roles").isArray()) {
                        for (JsonNode roleNode : dataNode.get("roles")) {
                            roles.add(roleNode.asText());
                        }
                    }
                    if (roles.isEmpty()) {
                        roles.add("CUSTOMER");
                    }

                    String token = jwtUtil.generateToken(userId, email, roles);

                    ResponseCookie cookie = ResponseCookie.from("jwt", token)
                            .maxAge(Duration.ofMillis(expiration))
                            .httpOnly(true)
                            .secure(false)
                            .path("/")
                            .sameSite("Strict")
                            .build();

                    response.addCookie(cookie);
                    response.getHeaders().add("X-Auth-Token", token);

                    ObjectNode modifiedJson = objectMapper.createObjectNode();
                    modifiedJson.put("success", true);
                    modifiedJson.put("message", "Login successful");
                    modifiedJson.put("token", token);
                    modifiedJson.put("userId", userId);
                    modifiedJson.put("email", email);
                    modifiedJson.set("roles", objectMapper.valueToTree(roles));
                    modifiedJson.set("data", dataNode);

                    return Mono.just(objectMapper.writeValueAsString(modifiedJson));
                } else {
                    return Mono.just(body);
                }
            } catch (Exception e) {
                System.err.println("Error modifying login response: " + e.getMessage());
                e.printStackTrace();
                return Mono.just(body);
            }
        }

        return Mono.just(body);
    }
}
