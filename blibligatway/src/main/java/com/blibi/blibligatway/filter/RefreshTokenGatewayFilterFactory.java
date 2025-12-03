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
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Filter factory for refreshing access tokens using refresh tokens.
 * Validates refresh token and issues new access + refresh token pair.
 */
@Slf4j
@Component
public class RefreshTokenGatewayFilterFactory extends AbstractGatewayFilterFactory<RefreshTokenGatewayFilterFactory.Config> {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;
    private final ObjectMapper objectMapper;
    
    @Value("${jwt.expiration}")
    private long expiration;
    
    @Value("${jwt.cookie.secure:false}")
    private boolean cookieSecure;

    public RefreshTokenGatewayFilterFactory(JwtUtil jwtUtil, TokenBlacklistService tokenBlacklistService, 
                                            ObjectMapper objectMapper) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
        this.objectMapper = objectMapper;
    }

    public static class Config {}

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            log.info("RefreshToken filter - processing token refresh request");
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            // Extract refresh token from request
            String refreshToken = extractRefreshTokenFromRequest(request);

            if (!StringUtils.hasText(refreshToken)) {
                log.warn("Refresh token missing");
                return errorResponse(exchange, HttpStatus.BAD_REQUEST, "Refresh token is required");
            }

            // Check if refresh token is blacklisted
            if (tokenBlacklistService.isBlacklisted(refreshToken)) {
                log.warn("Refresh token is blacklisted");
                return errorResponse(exchange, HttpStatus.UNAUTHORIZED, "Refresh token has been revoked");
            }

            // Validate refresh token
            if (!jwtUtil.validateToken(refreshToken)) {
                log.warn("Invalid or expired refresh token");
                return errorResponse(exchange, HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token");
            }

            // Check if it's actually a refresh token
            if (!jwtUtil.isRefreshToken(refreshToken)) {
                log.warn("Token provided is not a refresh token");
                return errorResponse(exchange, HttpStatus.BAD_REQUEST, "Token is not a refresh token");
            }

            // Extract user information from refresh token
            Claims claims;
            try {
                claims = jwtUtil.getAllClaimsFromToken(refreshToken);
            } catch (Exception e) {
                log.error("Error extracting claims from refresh token: {}", e.getMessage());
                return errorResponse(exchange, HttpStatus.UNAUTHORIZED, "Invalid token format");
            }

            String userId = claims.getSubject();
            if (userId == null) {
                log.error("Refresh token missing user ID");
                return errorResponse(exchange, HttpStatus.UNAUTHORIZED, "Invalid token: missing user information");
            }

            // Get user email and roles from refresh token (if available) or use defaults
            String email = claims.get("email", String.class);
            List<String> roles = new ArrayList<>();
            if (claims.get("roles") != null) {
                @SuppressWarnings("unchecked")
                List<String> tokenRoles = (List<String>) claims.get("roles");
                roles.addAll(tokenRoles);
            } else {
                // Default role if not present
                roles.add("CUSTOMER");
            }

            // Generate new access token
            String newAccessToken = jwtUtil.generateToken(userId, email != null ? email : "", roles);
            
            // Generate new refresh token (rotate refresh token for security)
            String newRefreshToken = jwtUtil.generateRefreshToken(userId);

            // Blacklist old refresh token
            long expirationTime = claims.getExpiration().getTime();
            tokenBlacklistService.blacklistToken(refreshToken, expirationTime);
            log.info("Old refresh token blacklisted for user: {}", userId);

            // Set new JWT cookie
            long cookieMaxAge = expiration / 1000;
            ResponseCookie jwtCookie = ResponseCookie.from("jwt", newAccessToken)
                    .maxAge(Duration.ofSeconds(cookieMaxAge))
                    .httpOnly(true)
                    .secure(cookieSecure)
                    .path("/")
                    .sameSite("Strict")
                    .build();

            // Set new refresh token cookie
            long refreshCookieMaxAge = jwtUtil.getRefreshExpiration() / 1000;
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", newRefreshToken)
                    .maxAge(Duration.ofSeconds(refreshCookieMaxAge))
                    .httpOnly(true)
                    .secure(cookieSecure)
                    .path("/")
                    .sameSite("Strict")
                    .build();

            response.addCookie(jwtCookie);
            response.addCookie(refreshCookie);
            response.setStatusCode(HttpStatus.OK);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

            // Build success response
            try {
                ObjectNode jsonResponse = objectMapper.createObjectNode();
                jsonResponse.put("success", true);
                jsonResponse.put("message", "Token refreshed successfully");
                jsonResponse.put("token", newAccessToken);
                jsonResponse.put("refreshToken", newRefreshToken);
                jsonResponse.put("userId", userId);
                jsonResponse.put("timestamp", Instant.now().toString());

                String responseBody = objectMapper.writeValueAsString(jsonResponse);
                DataBuffer buffer = response.bufferFactory()
                        .wrap(responseBody.getBytes(StandardCharsets.UTF_8));

                return response.writeWith(Mono.just(buffer));
            } catch (Exception e) {
                log.error("Error creating refresh response: {}", e.getMessage(), e);
                return errorResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR, "Error refreshing token");
            }
        };
    }

    /**
     * Extract refresh token from request (header or cookie)
     */
    private String extractRefreshTokenFromRequest(ServerHttpRequest request) {
        // Try Authorization header first
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // Try refreshToken cookie
        HttpCookie refreshCookie = request.getCookies().getFirst("refreshToken");
        if (refreshCookie != null && StringUtils.hasText(refreshCookie.getValue())) {
            return refreshCookie.getValue();
        }

        return null;
    }

    /**
     * Return error response
     */
    private Mono<Void> errorResponse(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            ObjectNode errorResponse = objectMapper.createObjectNode();
            errorResponse.put("success", false);
            errorResponse.put("message", message);
            errorResponse.put("timestamp", Instant.now().toString());
            errorResponse.put("status", status.value());

            String responseBody = objectMapper.writeValueAsString(errorResponse);
            DataBuffer buffer = response.bufferFactory()
                    .wrap(responseBody.getBytes(StandardCharsets.UTF_8));

            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            log.error("Error creating error response: {}", e.getMessage());
            String errorResponse = String.format("{\"success\":false,\"message\":\"%s\"}", message);
            DataBuffer buffer = response.bufferFactory()
                    .wrap(errorResponse.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        }
    }
}

