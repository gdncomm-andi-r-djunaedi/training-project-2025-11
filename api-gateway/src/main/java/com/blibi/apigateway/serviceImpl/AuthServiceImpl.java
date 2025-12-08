package com.blibi.apigateway.serviceImpl;

import com.blibi.apigateway.dto.GenericResponse;
import com.blibi.apigateway.dto.LoginRequest;
import com.blibi.apigateway.dto.LoginResponse;
import com.blibi.apigateway.dto.MemberValidationRequest;
import com.blibi.apigateway.dto.MemberValidationResponse;
import com.blibi.apigateway.dto.TokenValidationResponse;
import com.blibi.apigateway.exception.UnauthorizedException;
import com.blibi.apigateway.service.AuthService;
import com.blibi.apigateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Authentication service implementation
 * Handles login and logout operations with JWT token generation
 * Uses WebClient for direct HTTP calls to Member service (no Feign)
 * 
 * Topics to be learned: WebClient, Reactive Programming, JWT, Redis
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final WebClient.Builder webClientBuilder;

    @Value("${member.service.url:http://localhost:8083}")
    private String memberServiceUrl;

    /**
     * Authenticate user and generate JWT token
     * 
     * @param request Login request with username and password
     * @return Login response with JWT token and user information
     * @throws UnauthorizedException if credentials are invalid
     */
    @Override
    public Mono<LoginResponse> login(LoginRequest request) {
        log.info("Processing login request for user: {}", request.getUserName());

        // Call Member service to validate credentials
        MemberValidationRequest validationRequest = MemberValidationRequest.builder()
                .userName(request.getUserName())
                .password(request.getPassword())
                .build();

        WebClient webClient = webClientBuilder.baseUrl(memberServiceUrl).build();

        return webClient.post()
                .uri("/api/member/login")
                .header("Content-Type", "application/json")
                .bodyValue(validationRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<GenericResponse<MemberValidationResponse>>() {
                })
                .doOnError(error -> log.error("WebClient error calling member service", error))
                .flatMap(memberResponse -> {
                    if (memberResponse == null || memberResponse.getData() == null) {
                        log.error("Invalid response from member service for user: {}", request.getUserName());
                        return Mono.error(new UnauthorizedException("Authentication failed"));
                    }

                    MemberValidationResponse memberData = memberResponse.getData();

                    // Generate JWT token with additional claims
                    Map<String, Object> claims = new HashMap<>();
                    claims.put("userId", memberData.getUserId().toString());
                    claims.put("email", memberData.getEmail());
                    claims.put("active", memberData.isActive());

                    String token = jwtUtil.generateToken(request.getUserName(), claims);

                    log.info("Login successful for user: {}", request.getUserName());

                    return Mono.just(LoginResponse.builder()
                            .token(token)
                            .userName(memberData.getUserName())
                            .build());
                })
                .onErrorMap(e -> {
                    if (e instanceof UnauthorizedException) {
                        return e;
                    }
                    log.error("Login failed for user: {} - Error: {}", request.getUserName(), e.getMessage(), e);
                    return new UnauthorizedException("Invalid credentials");
                });
    }

    /**
     * Logout - invalidate JWT token by adding to Redis blacklist
     *
     * @param token JWT token to invalidate
     */
    @Override
    public void logout(String token) {
        log.info("Invalidating token");

        // Calculate TTL based on token expiration
        try {
            Claims claims = jwtUtil.validateAndGetClaims(token);
            Date expiration = claims.getExpiration();
            long ttlSeconds = (expiration.getTime() - System.currentTimeMillis()) / 1000;

            if (ttlSeconds > 0) {
                // Add token to blacklist with TTL matching token expiration
                redisTemplate.opsForValue().set("invalid:" + token, "true", ttlSeconds, TimeUnit.SECONDS);
                log.info("Token blacklisted with TTL: {} seconds", ttlSeconds);
            }
        } catch (Exception e) {
            log.error("Error calculating token TTL, using default", e);
            redisTemplate.opsForValue().set("invalid:" + token, "true");
        }
    }

    /**
     * Validate JWT token
     *
     * @param token JWT token to validate
     * @return TokenValidationResponse with validation status and claims
     */
    @Override
    public TokenValidationResponse validateToken(String token) {
        log.info("Validating token");

        try {
            // Check if token is blacklisted
            Boolean isBlacklisted = redisTemplate.hasKey("invalid:" + token);
            if (Boolean.TRUE.equals(isBlacklisted)) {
                log.warn("Token is blacklisted");
                return TokenValidationResponse.builder()
                        .valid(false)
                        .message("Token has been invalidated")
                        .build();
            }

            // Validate token and extract claims
            Claims claims = jwtUtil.validateAndGetClaims(token);
            String username = jwtUtil.extractUsername(token);

            // Check if token is expired
            if (jwtUtil.isTokenExpired(token)) {
                log.warn("Token is expired");
                return TokenValidationResponse.builder()
                        .valid(false)
                        .message("Token has expired")
                        .build();
            }

            // Convert claims to Map
            Map<String, Object> claimsMap = new HashMap<>();
            claims.forEach((key, value) -> claimsMap.put(key, value));

            log.info("Token is valid for user: {}", username);
            return TokenValidationResponse.builder()
                    .valid(true)
                    .username(username)
                    .claims(claimsMap)
                    .message("Token is valid")
                    .build();

        } catch (Exception e) {
            log.error("Token validation failed", e);
            return TokenValidationResponse.builder()
                    .valid(false)
                    .message("Invalid token: " + e.getMessage())
                    .build();
        }
    }
}
