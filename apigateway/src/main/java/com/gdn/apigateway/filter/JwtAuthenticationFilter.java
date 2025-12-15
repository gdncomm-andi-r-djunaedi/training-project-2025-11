package com.gdn.apigateway.filter;

import com.gdn.apigateway.service.TokenBlacklistService;
import com.gdn.apigateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

  private static final String TOKEN_COOKIE_NAME = "token";

  private final JwtUtil jwtUtil;
  private final TokenBlacklistService tokenBlacklistService;

  // Public endpoints that don't require authentication
  private static final List<String> PUBLIC_ENDPOINTS = List.of(
      "/members/login",
      "/members/register",
      "/products"
  );

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();
    String path = request.getPath().value();

    // Skip authentication for public endpoints
    if (isPublicEndpoint(path)) {
      return chain.filter(exchange);
    }

    // Get token from header or cookie
    String token = extractToken(request);

    if (token == null) {
      return onUnauthorized(exchange, "Missing or invalid Authorization header or cookie");
    }

    // Check if token is blacklisted
    return tokenBlacklistService.isBlacklisted(token)
        .flatMap(isBlacklisted -> {
          if (isBlacklisted) {
            log.warn("Attempt to use blacklisted token");
            return onUnauthorized(exchange, "Token has been revoked");
          }

          try {
            // Validate token and extract claims
            Claims claims = jwtUtil.validateTokenAndGetClaims(token);
            String username = claims.getSubject();
            String memberId = claims.getId();

            // Add user info to headers for downstream services
            ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Id", memberId)
                .header("X-Username", username)
                .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

          } catch (Exception e) {
            log.error("JWT validation failed: {}", e.getMessage());
            return onUnauthorized(exchange, "Invalid or expired token");
          }
        });
  }

  /**
   * Extract token from Authorization header or cookie
   */
  private String extractToken(ServerHttpRequest request) {
    // Try Authorization header first
    String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }

    // Try cookie
    HttpCookie cookie = request.getCookies().getFirst(TOKEN_COOKIE_NAME);
    if (cookie != null && !cookie.getValue().isBlank()) {
      return cookie.getValue();
    }

    return null;
  }

  private boolean isPublicEndpoint(String path) {
    return PUBLIC_ENDPOINTS.stream()
        .anyMatch(endpoint -> path.startsWith(endpoint));
  }

  private Mono<Void> onUnauthorized(ServerWebExchange exchange, String message) {
    ServerHttpResponse response = exchange.getResponse();
    response.setStatusCode(HttpStatus.UNAUTHORIZED);
    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

    String body = String.format("{\"error\": \"Unauthorized\", \"message\": \"%s\"}", message);
    DataBuffer buffer = response.bufferFactory()
        .wrap(body.getBytes(StandardCharsets.UTF_8));

    return response.writeWith(Mono.just(buffer));
  }

  @Override
  public int getOrder() {
    return -100; // High priority to run early in the filter chain
  }
}

