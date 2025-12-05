package com.dev.onlineMarketplace.gateway.controller;

import com.dev.onlineMarketplace.gateway.dto.LoginRequestDTO;
import com.dev.onlineMarketplace.gateway.dto.LoginResponseDTO;
import com.dev.onlineMarketplace.gateway.response.GdnResponseData;
import com.dev.onlineMarketplace.gateway.service.TokenBlacklistService;
import com.dev.onlineMarketplace.gateway.util.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

/**
 * Authentication Controller (Gateway)
 * 
 * Handles login and logout:
 * 1. Login: Forwards to MemberService for validation, generates JWT in Gateway
 * 2. Logout: Validates JWT and can add to blacklist
 * 
 * Note: Registration is handled directly by MemberService via Gateway routing
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication (Gateway)", description = "Gateway authentication - Login generates JWT tokens here")
public class AuthProxyController {

    private static final Logger logger = LoggerFactory.getLogger(AuthProxyController.class);

    private final WebClient webClient;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthProxyController(
            @Value("${member.service.url:http://localhost:8061}") String memberServiceUrl,
            JwtUtil jwtUtil,
            ObjectMapper objectMapper,
            TokenBlacklistService tokenBlacklistService) {
        this.webClient = WebClient.builder().baseUrl(memberServiceUrl).build();
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostMapping("/login")
    @Operation(
        summary = "Login and get JWT tokens (Public - No Auth Required)", 
        description = "Gateway forwards credentials to Member Service for validation, then generates JWT tokens",
        security = {} // This endpoint is public - no security required
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful - JWT tokens returned"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public Mono<String> login(@Valid @RequestBody LoginRequestDTO request) {
        logger.info("[Gateway] POST /api/v1/auth/login - Username: {}", request.getUsername());

        // Forward to Member Service for credential validation
        return webClient.post()
                .uri("/api/v1/member/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(response -> logger.info("[Gateway] Member Service raw response: {}", response))
                .flatMap(response -> {
                    try {
                        // Parse response from Member Service
                        JsonNode jsonNode = objectMapper.readTree(response);
                        logger.info("[Gateway] Parsed JSON - status: {}, message: {}, data: {}", 
                                jsonNode.path("status").asInt(),
                                jsonNode.path("message").asText(),
                                jsonNode.path("data").toString());
                        
                        // Check status code from Member Service (200 = success)
                        int status = jsonNode.path("status").asInt(0);
                        String message = jsonNode.path("message").asText("");
                        
                        // Member Service returns status 200 for successful login
                        if (status != 200) {
                            logger.warn("[Gateway] Member Service returned error: status={}, message={}", status, message);
                            return Mono.just(String.format("{\"success\":false,\"message\":\"%s\",\"status\":%d}", 
                                    message.isEmpty() ? "Invalid credentials" : message, 
                                    status == 0 ? 401 : status));
                        }

                        // Extract data node
                        JsonNode dataNode = jsonNode.path("data");
                        if (dataNode.isMissingNode() || dataNode.isNull()) {
                            logger.error("[Gateway] Member Service response missing 'data' field");
                            return Mono.just("{\"success\":false,\"message\":\"Invalid response from authentication service\",\"status\":500}");
                        }

                        // Extract username from response (Member Service returns user details)
                        String username = dataNode.path("username").asText();
                        if (username == null || username.isEmpty()) {
                            // Try email field as fallback
                            username = dataNode.path("email").asText();
                        }
                        if (username == null || username.isEmpty()) {
                            username = request.getUsername(); // Final fallback to request username
                        }

                        logger.info("[Gateway] Extracted username: {}", username);

                        // Generate JWT tokens in Gateway
                        String accessToken = jwtUtil.generateAccessToken(username);
                        String refreshToken = jwtUtil.generateRefreshToken(username);

                        LoginResponseDTO loginResponse = new LoginResponseDTO(
                                accessToken,
                                refreshToken,
                                "Bearer",
                                jwtUtil.getAccessTokenExpiration() / 1000
                        );

                        logger.info("[Gateway] Login successful for username: {} - JWT tokens generated", username);
                        
                        GdnResponseData<LoginResponseDTO> result = GdnResponseData.success(
                                loginResponse, "Login successful - Tokens generated by Gateway");
                        return Mono.just(objectMapper.writeValueAsString(result));

                    } catch (Exception e) {
                        logger.error("[Gateway] Failed to process login response", e);
                        return Mono.just("{\"success\":false,\"message\":\"Failed to process login\",\"status\":500}");
                    }
                })
                .onErrorResume(WebClientResponseException.class, ex -> {
                    logger.error("[Gateway] Login failed from Member Service: Status={}, Body={}", 
                            ex.getStatusCode(), ex.getResponseBodyAsString());
                    
                    // Try to parse error response from Member Service
                    try {
                        JsonNode errorNode = objectMapper.readTree(ex.getResponseBodyAsString());
                        String errorMessage = errorNode.path("message").asText("Invalid credentials");
                        return Mono.just(String.format("{\"success\":false,\"message\":\"%s\",\"status\":401}", errorMessage));
                    } catch (Exception e) {
                        return Mono.just("{\"success\":false,\"message\":\"Invalid credentials\",\"status\":401}");
                    }
                })
                .onErrorResume(ex -> {
                    logger.error("[Gateway] Login error", ex);
                    return Mono.just("{\"success\":false,\"message\":\"Login failed\",\"status\":500}");
                });
    }

    @PostMapping("/logout")
    @Operation(
        summary = "Logout (Public - No Auth Required for Request, but validates token)", 
        description = "Validates JWT token and invalidates it (can be extended with token blacklisting)",
        security = {} // Public endpoint - but token is checked if provided
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logged out successfully"),
        @ApiResponse(responseCode = "401", description = "Invalid or missing token")
    })
    public Mono<String> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        logger.info("[Gateway] POST /api/v1/auth/logout");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("[Gateway] Logout failed: Missing or invalid Authorization header");
            return Mono.just("{\"success\":false,\"message\":\"Authorization token required\",\"status\":401}");
        }

        String token = authHeader.substring(7);

        // Validate token
        if (!jwtUtil.validateToken(token)) {
            logger.warn("[Gateway] Logout failed: Invalid token");
            return Mono.just("{\"success\":false,\"message\":\"Invalid token\",\"status\":401}");
        }

        // Add token to blacklist (Redis) for invalidation
        return tokenBlacklistService.blacklistToken(token)
                .flatMap(success -> {
                    if (Boolean.TRUE.equals(success)) {
                        logger.info("[Gateway] Logout successful - Token blacklisted");
                        return Mono.just("{\"success\":true,\"message\":\"Logged out successfully\",\"data\":\"Token has been invalidated\",\"status\":200}");
                    } else {
                        logger.error("[Gateway] Failed to blacklist token");
                        return Mono.just("{\"success\":false,\"message\":\"Logout failed - Unable to invalidate token\",\"status\":500}");
                    }
                })
                .onErrorResume(error -> {
                    logger.error("[Gateway] Logout error", error);
                    return Mono.just("{\"success\":false,\"message\":\"Logout failed\",\"status\":500}");
                });
    }
}
