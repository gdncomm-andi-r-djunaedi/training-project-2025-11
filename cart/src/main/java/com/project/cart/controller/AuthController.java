package com.project.cart.controller;

import com.project.cart.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication controller for generating JWT tokens
 * In production, integrate with proper authentication service
 */
@Slf4j
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "APIs for authentication")
public class AuthController {

    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    @Operation(summary = "Login and get JWT token",
            description = "Generates JWT token for testing (simplified auth)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for user: {}", request.getUsername());

        // In production, validate credentials against database
        // For demo, accept any username
        String token = jwtUtil.generateToken(request.getUsername());

        LoginResponse response = LoginResponse.builder()
                .token(token)
                .type("Bearer")
                .username(request.getUsername())
                .build();

        return ResponseEntity.ok(response);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Login request")
    public static class LoginRequest {

        @NotBlank(message = "Username is required")
        @Schema(description = "Username", example = "user123")
        private String username;

        @Schema(description = "Password (not validated in demo)", example = "password")
        private String password;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Login response with JWT token")
    public static class LoginResponse {

        @Schema(description = "JWT token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        private String token;

        @Schema(description = "Token type", example = "Bearer")
        private String type;

        @Schema(description = "Username", example = "user123")
        private String username;
    }
}
