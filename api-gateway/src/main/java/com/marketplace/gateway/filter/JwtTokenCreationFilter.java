package com.marketplace.gateway.filter;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.marketplace.gateway.config.JwtProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtTokenCreationFilter {

    private final JwtProperties jwtProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtTokenCreationFilter(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public Mono<String> modifyResponse(ServerWebExchange exchange, String originalBody) {
        try {
            log.info("JwtTokenCreationFilter: Processing login response. Body length: {}",
                    originalBody != null ? originalBody.length() : 0);

            if (originalBody == null || originalBody.isEmpty()) {
                log.warn("Empty response body received");
                return Mono.just(originalBody);
            }

            log.debug("Original response body: {}", originalBody);
            JsonNode jsonNode = objectMapper.readTree(originalBody);

            JsonNode dataNode = jsonNode.has("data") ? jsonNode.get("data") : jsonNode;
            String userId = null;
            String email = null;

            if (dataNode != null && dataNode.has("id")) {
                userId = dataNode.get("id").asText();
                email = extractEmail(dataNode);
                log.info("Extracted userId: {}, email: {}", userId, email);
            } else if (jsonNode.has("id")) {
                userId = jsonNode.get("id").asText();
                email = extractEmail(jsonNode);
                log.info("Extracted userId: {}, email: {} (direct structure)", userId, email);
            }

            if (userId != null) {
                String token = createJwtToken(userId, email);
                log.info("JWT token created successfully. Token length: {}", token.length());

                ObjectNode responseNode = objectMapper.createObjectNode();
                if (jsonNode.isObject()) {
                    jsonNode.fields().forEachRemaining(entry ->
                            responseNode.set(entry.getKey(), entry.getValue())
                    );
                }

                responseNode.put("token", token);
                responseNode.put("tokenType", "Bearer");
                responseNode.put("expiresIn", jwtProperties.getExpirationSeconds());

                setSecureCookie(exchange.getResponse(), token);
                String finalResponse = objectMapper.writeValueAsString(responseNode);
                log.info("JWT token added to response. Final response length: {}", finalResponse.length());
                return Mono.just(finalResponse);
            }

            java.util.List<String> fieldNames = new java.util.ArrayList<>();
            jsonNode.fieldNames().forEachRemaining(fieldNames::add);
            log.warn("Login response does not contain 'id' field. Available fields: {}",
                    fieldNames.isEmpty() ? "none" : String.join(", ", fieldNames));
            return Mono.just(originalBody);
        } catch (Exception e) {
            log.error("Error processing login response: {}", e.getMessage(), e);
            return Mono.just(originalBody);
        }
    }

    private String createJwtToken(String userId, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("user_id", userId);
        if (email != null) claims.put("email", email);

        Date now = new Date();
        SecretKey secretKey = Keys.hmacShaKeyFor(
            jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8)
        );

        return Jwts.builder()
                .subject(userId)
                .claims(claims)
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + jwtProperties.getExpirationSeconds() * 1000))
                .signWith(secretKey)
                .compact();
    }

    private void setSecureCookie(org.springframework.http.server.reactive.ServerHttpResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(jwtProperties.getCookieName(), token)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(jwtProperties.getExpirationSeconds())
                .build();
        response.addCookie(cookie);
    }

    private String extractEmail(JsonNode jsonNode) {
        return jsonNode.has("email") ? jsonNode.get("email").asText() : null;
    }
}
