package com.blibi.apigateway.serviceImpl;

import com.blibi.apigateway.dto.GenericResponse;
import com.blibi.apigateway.dto.LoginRequest;
import com.blibi.apigateway.dto.LoginResponse;
import com.blibi.apigateway.dto.MemberValidationRequest;
import com.blibi.apigateway.dto.MemberValidationResponse;
import com.blibi.apigateway.exception.UnauthorizedException;
import com.blibi.apigateway.service.AuthService;
import com.blibi.apigateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
     * Logout user and invalidate JWT token
     * Token is added to Redis blacklist with TTL matching token expiration
     * 
     * @param token JWT token to invalidate
     */
    @Override
    public void logout(String token) {
        log.info("Processing logout request");

        try {
            // Add token to blacklist in Redis
            // Set TTL to 24 hours (matching token expiration)
            redisTemplate.opsForValue().set(
                    "invalid:" + token,
                    "true",
                    24,
                    TimeUnit.HOURS);

            log.info("Token invalidated successfully");
        } catch (Exception e) {
            log.error("Failed to invalidate token", e);
            // Don't throw exception, logout should succeed even if Redis fails
        }
    }
}
