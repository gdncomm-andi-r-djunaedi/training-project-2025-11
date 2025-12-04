package com.blibi.apigateway.controller;

import com.blibi.apigateway.configuration.JwtProperties;
import com.blibi.apigateway.dto.GenericResponse;
import com.blibi.apigateway.dto.LoginRequest;
import com.blibi.apigateway.dto.LoginResponse;
import com.blibi.apigateway.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Authentication controller for login and logout operations
 * Supports both Bearer token and cookie-based authentication
 * 
 * Topics to be learned: JWT, Cookies, HTTP Headers, Spring WebFlux
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtProperties jwtProperties;

    /**
     * Login endpoint - authenticates user and returns JWT token
     * Token is returned both in response body and as HTTP-only cookie
     * 
     * @param request  Login request with username and password
     * @param exchange Server web exchange for setting cookies
     * @return Generic response with login data and JWT token
     */
    @PostMapping("/login")
    public Mono<ResponseEntity<GenericResponse<LoginResponse>>> login(
            @RequestBody LoginRequest request,
            ServerWebExchange exchange) {
        log.info("Login request received for user: {}", request.getUserName());

        return authService.login(request)
                .map(loginResponse -> {
                    // Create JWT cookie
                    ResponseCookie jwtCookie = ResponseCookie
                            .from(jwtProperties.getCookieName(), loginResponse.getToken())
                            .httpOnly(jwtProperties.isCookieHttpOnly())
                            .secure(jwtProperties.isCookieSecure())
                            .path(jwtProperties.getCookiePath())
                            .maxAge(jwtProperties.getCookieMaxAge())
                            .sameSite(jwtProperties.getCookieSameSite())
                            .build();

                    // Add cookie to response
                    exchange.getResponse().addCookie(jwtCookie);

                    log.info("Login successful for user: {}", request.getUserName());

                    GenericResponse<LoginResponse> response = GenericResponse.<LoginResponse>builder()
                            .status("SUCCESS")
                            .message("Login successful")
                            .data(loginResponse)
                            .build();

                    return ResponseEntity.ok(response);
                })
                .onErrorResume(e -> {
                    log.error("Login failed", e);
                    GenericResponse<LoginResponse> errorResponse = GenericResponse.<LoginResponse>builder()
                            .status("ERROR")
                            .message(e.getMessage())
                            .data(null)
                            .build();
                    return Mono.just(ResponseEntity.ok(errorResponse));
                });
    }

    /**
     * Logout endpoint - invalidates JWT token
     * Clears both Bearer token and cookie
     * 
     * @param authHeader Authorization header with Bearer token (optional)
     * @param exchange   Server web exchange for clearing cookies
     * @return Generic response confirming logout
     */
    @PostMapping("/logout")
    public ResponseEntity<GenericResponse<String>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            ServerWebExchange exchange) {

        log.info("Logout request received");

        // Extract token from Authorization header or cookie
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.replace("Bearer ", "");
        } else {
            // Try to get token from cookie
            var cookies = exchange.getRequest().getCookies();
            if (cookies.containsKey(jwtProperties.getCookieName())) {
                var cookieList = cookies.get(jwtProperties.getCookieName());
                if (cookieList != null && !cookieList.isEmpty()) {
                    token = cookieList.get(0).getValue();
                }
            }
        }

        if (token != null) {
            authService.logout(token);
        }

        // Clear cookie by setting max age to 0
        ResponseCookie clearCookie = ResponseCookie.from(jwtProperties.getCookieName(), "")
                .httpOnly(true)
                .secure(jwtProperties.isCookieSecure())
                .path(jwtProperties.getCookiePath())
                .maxAge(0)
                .sameSite(jwtProperties.getCookieSameSite())
                .build();

        exchange.getResponse().addCookie(clearCookie);

        log.info("Logout successful");

        return ResponseEntity.ok()
                .body(GenericResponse.<String>builder()
                        .status("SUCCESS")
                        .message("Logged out and token invalidated")
                        .data("OK")
                        .build());
    }
}
