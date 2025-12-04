package com.blibi.blibligatway.filter;

import com.blibi.blibligatway.security.JwtUtil;
import com.blibi.blibligatway.security.TokenBlacklistService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

@Slf4j
@Component
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthGatewayFilterFactory.Config> {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;
    private final ObjectMapper objectMapper;
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String GATEWAY_SIGNATURE_HEADER = "X-Gateway-Signature";
    
    @Value("${gateway.secret:GatewaySecretKeyForServiceVerification2024}")
    private String gatewaySecret;

    public AuthGatewayFilterFactory(JwtUtil jwtUtil, TokenBlacklistService tokenBlacklistService, ObjectMapper objectMapper) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
        this.objectMapper = objectMapper;
    }

    public static class Config {}

    @Override
    public String name() {
        return "Auth";
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().toString();
            String method = request.getMethod().toString();

            log.debug("Auth filter - Processing request: {} {}", method, path);

            String token = extractJwtFromRequest(request);

            // Check if token is missing
            if (!StringUtils.hasText(token)) {
                log.warn("Authentication failed - Missing token. Path: {}, IP: {}", 
                    path, request.getRemoteAddress());
                return unauthorized(exchange, "Missing authentication token");
            }

          // check token blacklisted
            if (tokenBlacklistService.isBlacklisted(token)) {
                log.warn("Authentication failed - Token blacklisted. Path: {}, IP: {}", 
                    path, request.getRemoteAddress());
                return unauthorized(exchange, "Token has been revoked");
            }

            // Validate token
            if (!jwtUtil.validateToken(token)) {
                log.warn("Authentication failed - Invalid or expired token. Path: {}, IP: {}", 
                    path, request.getRemoteAddress());
                return unauthorized(exchange, "Invalid or expired token");
            }

            // Extract claims
            Claims claims;
            try {
                claims = jwtUtil.getAllClaimsFromToken(token);
            } catch (Exception e) {
                log.error("Error extracting claims from token: {}", e.getMessage());
                return unauthorized(exchange, "Invalid token format");
            }

            // Check if it's a refresh token (should not be used for authentication)
            if (jwtUtil.isRefreshToken(token)) {
                log.warn("Authentication failed - Refresh token used for authentication. Path: {}", path);
                return unauthorized(exchange, "Refresh token cannot be used for authentication");
            }

            String userId = claims.getSubject();
            String email = claims.get("email", String.class);
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) claims.get("roles");

            if (userId == null) {
                log.error("Token missing user ID in subject");
                return unauthorized(exchange, "Invalid token: missing user information");
            }

            log.debug("Authentication successful - User: {}, Path: {}", userId, path);

            // Generate gateway signature to verify request came from gateway
            String gatewaySignature = generateGatewaySignature(userId, path);

            ServerHttpRequest modifiedRequest = request.mutate()
                    .header(USER_ID_HEADER, userId)
                    .header("X-User-Email", email != null ? email : "")
                    .header("X-User-Roles", roles != null ? String.join(",", roles) : "")
                    .header(GATEWAY_SIGNATURE_HEADER, gatewaySignature)
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String reason) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            ObjectNode errorResponse = objectMapper.createObjectNode();
            errorResponse.put("success", false);
            errorResponse.put("message", "Authentication failed");
            errorResponse.put("error", reason);
            errorResponse.put("timestamp", Instant.now().toString());
            errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());

            String responseBody = objectMapper.writeValueAsString(errorResponse);
            DataBuffer buffer = response.bufferFactory()
                    .wrap(responseBody.getBytes(StandardCharsets.UTF_8));

            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            log.error("Error creating error response: {}", e.getMessage());
            String errorResponse = String.format(
                "{\"success\":false,\"message\":\"Authentication failed\",\"error\":\"%s\"}", 
                reason);
            DataBuffer buffer = response.bufferFactory()
                    .wrap(errorResponse.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        }
    }

    private String extractJwtFromRequest(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        HttpCookie jwtCookie = request.getCookies().getFirst("jwt");
        if (jwtCookie != null && StringUtils.hasText(jwtCookie.getValue())) {
            return jwtCookie.getValue();
        }

        return null;
    }

    /**
     * Generate gateway signature to prove request came from gateway
     * Services can verify this signature to ensure request authenticity
     */
    private String generateGatewaySignature(String userId, String path) {
        try {
            // Create signature based on userId, path, and secret
            String dataToSign = userId + "|" + path + "|" + gatewaySecret;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(dataToSign.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("Error generating gateway signature: {}", e.getMessage());
            // Fallback: simple hash
            return String.valueOf((userId + path + gatewaySecret).hashCode());
        }
    }
}
