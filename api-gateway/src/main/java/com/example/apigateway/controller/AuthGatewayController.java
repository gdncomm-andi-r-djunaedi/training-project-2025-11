package com.example.apigateway.controller;

import com.example.apigateway.common.ApiResponse;
import com.example.apigateway.dto.LoginRequest;
import com.example.apigateway.dto.RegisterRequest;
import com.example.apigateway.model.Token;
import com.example.apigateway.repository.TokenRepository;
import com.example.apigateway.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication")
public class AuthGatewayController {

    private final RestTemplate restTemplate;
    private final String memberServiceBaseUrl;
    private final JwtUtil jwtUtil;
    private final TokenRepository tokenRepository;

    public AuthGatewayController(RestTemplate restTemplate,
                                 @Value("${member.service.base-url}") String memberServiceBaseUrl,
                                 JwtUtil jwtUtil, TokenRepository tokenRepository) {
        this.restTemplate = restTemplate;
        this.memberServiceBaseUrl = memberServiceBaseUrl;
        this.jwtUtil = jwtUtil;
        this.tokenRepository = tokenRepository;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        ResponseEntity<Object> response = restTemplate.postForEntity(
                memberServiceBaseUrl + "/api/auth/register",
                request,
                Object.class
        );

        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(response.getStatusCode().value())
                .status(HttpStatus.valueOf(response.getStatusCode().value()).name())
                .data(response.getBody())
                .errors(null)
                .build();

        return ResponseEntity.status(response.getStatusCode()).body(apiResponse);
    }

    @PostMapping("/login")
    @Operation(summary = "Login user and return a JWT token")
    public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody LoginRequest request) {

        ResponseEntity<Map> response = restTemplate.postForEntity(
                memberServiceBaseUrl + "/api/auth/login",
                request,
                Map.class
        );

        Map<String, Object> data = response.getBody();
        String userId = (String) data.get("id");
        String token = jwtUtil.generateToken(userId, Map.of("claims", data));

        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofMinutes(5));

        Token userToken = new Token();
        userToken.setToken(token);
        userToken.setMemberId(userId);
        userToken.setCreatedAt(now);
        userToken.setExpiresAt(expiresAt);

        tokenRepository.save(userToken);

        ApiResponse<?> result = ApiResponse.builder()
                .code(response.getStatusCode().value())
                .status(HttpStatus.valueOf(response.getStatusCode().value()).name())
                .data(Map.of(
                        "user", data,
                        "token", token
                ))
                .errors(null)
                .build();

        return ResponseEntity.ok(result);
    }
}