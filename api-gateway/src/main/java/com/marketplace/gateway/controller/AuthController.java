package com.marketplace.gateway.controller;

import com.marketplace.common.command.ReactiveCommandExecutor;
import com.marketplace.common.dto.ApiResponse;
import com.marketplace.gateway.command.LoginCommand;
import com.marketplace.gateway.dto.LoginRequest;
import com.marketplace.gateway.dto.LoginResponse;
import com.marketplace.gateway.util.CookieUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * Authentication controller for API Gateway.
 * Handles login and logout endpoints.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

        private final CookieUtil cookieUtil;
        private final ReactiveCommandExecutor commandExecutor;

        @Value("${jwt.expiration:86400000}")
        private Long jwtExpiration;

        /**
         * Login endpoint - validates credentials and returns JWT.
         */
        @PostMapping("/login")
        public Mono<ResponseEntity<ApiResponse<LoginResponse>>> login(
                        @Valid @RequestBody LoginRequest request,
                        ServerHttpResponse response) {

                log.info("Login request received for email: {}", request.getEmail());

                return commandExecutor.execute(LoginCommand.class, request)
                                .map(loginResponse -> {
                                        // Create secure cookie with JWT token
                                        ResponseCookie cookie = cookieUtil.createAuthCookie(
                                                        loginResponse.getToken(),
                                                        jwtExpiration);

                                        // Add cookie to response
                                        response.addCookie(cookie);

                                        log.info("Login successful for email: {}", loginResponse.getEmail());

                                        // Return JWT in both cookie AND response body
                                        return ResponseEntity.ok(
                                                        ApiResponse.success("Login successful", loginResponse));
                                })
                                .onErrorResume(error -> {
                                        log.error("Login failed: {}", error.getMessage());
                                        return Mono.just(
                                                        ResponseEntity
                                                                        .status(HttpStatus.UNAUTHORIZED)
                                                                        .body(ApiResponse
                                                                                        .error("Invalid credentials")));
                                });
        }

        /**
         * Logout endpoint - invalidates cookie.
         */
        @PostMapping("/logout")
        public Mono<ResponseEntity<ApiResponse<Void>>> logout(ServerHttpResponse response) {
                log.info("Logout request received");

                // Create cookie with Max-Age=0 to invalidate
                ResponseCookie logoutCookie = cookieUtil.createLogoutCookie();
                response.addCookie(logoutCookie);

                log.info("Logout successful - cookie invalidated");

                return Mono.just(
                                ResponseEntity.ok(ApiResponse.success("Logout successful", null)));
        }
}
