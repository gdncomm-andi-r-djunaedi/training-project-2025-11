package com.gdn.apigateway.controller;

import com.gdn.apigateway.service.TokenBlacklistService;
import com.gdn.apigateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

  private static final String TOKEN_COOKIE_NAME = "token";

  private final TokenBlacklistService tokenBlacklistService;
  private final JwtUtil jwtUtil;

  @PostMapping("/logout")
  public Mono<ResponseEntity<Map<String, String>>> logout(ServerWebExchange exchange) {
    // Extract token from header or cookie
    String token = extractToken(exchange);

    if (token == null) {
      return Mono.just(ResponseEntity
          .status(HttpStatus.BAD_REQUEST)
          .body(Map.of("error", "No token provided")));
    }

    try {
      // Get remaining time until token expires
      Duration ttl = jwtUtil.getRemainingTime(token);
      String username = jwtUtil.getUsernameFromToken(token);

      log.info("User {} is logging out", username);

      // Blacklist the token
      return tokenBlacklistService.blacklistToken(token, ttl)
          .map(success -> {
            // Clear the cookie
            ResponseCookie clearCookie = ResponseCookie.from(TOKEN_COOKIE_NAME, "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .secure(false) // Set to true in production with HTTPS
                .sameSite("Strict")
                .build();

            return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                .body(Map.of(
                    "message", "Logged out successfully",
                    "username", username
                ));
          });

    } catch (Exception e) {
      log.error("Logout failed: {}", e.getMessage());
      return Mono.just(ResponseEntity
          .status(HttpStatus.BAD_REQUEST)
          .body(Map.of("error", "Invalid token")));
    }
  }

  /**
   * Extract token from Authorization header or cookie
   */
  private String extractToken(ServerWebExchange exchange) {
    // Try Authorization header first
    String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }

    // Try cookie
    HttpCookie cookie = exchange.getRequest().getCookies().getFirst(TOKEN_COOKIE_NAME);
    if (cookie != null && !cookie.getValue().isBlank()) {
      return cookie.getValue();
    }

    return null;
  }
}

