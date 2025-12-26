package com.dev.onlineMarketplace.gateway.filter;

import com.dev.onlineMarketplace.gateway.service.TokenBlacklistService;
import com.dev.onlineMarketplace.gateway.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final RouteValidator routeValidator;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthenticationFilter(JwtUtil jwtUtil, RouteValidator routeValidator, TokenBlacklistService tokenBlacklistService) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
        this.routeValidator = routeValidator;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // Skip authentication for OPTIONS requests (CORS preflight)
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequest().getMethod().name())) {
                return chain.filter(exchange);
            }
            
            if (routeValidator.isSecured.test(exchange.getRequest())) {
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    return this.onError(exchange, "Missing authorization header", HttpStatus.UNAUTHORIZED);
                }

                String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                } else {
                    return this.onError(exchange, "Invalid authorization header", HttpStatus.UNAUTHORIZED);
                }

                final String token = authHeader;

                // Check if token is blacklisted
                return tokenBlacklistService.isTokenBlacklisted(token)
                        .flatMap(isBlacklisted -> {
                            if (Boolean.TRUE.equals(isBlacklisted)) {
                                logger.warn("Blacklisted token attempted to access: {}", exchange.getRequest().getURI());
                                return this.onError(exchange, "Token has been revoked", HttpStatus.UNAUTHORIZED);
                            }

                            // Validate token
                            try {
                                if (!jwtUtil.validateToken(token)) {
                                    return this.onError(exchange, "Invalid access token", HttpStatus.UNAUTHORIZED);
                                }
                                
                                // Extract username from token for rate limiting
                                String username = jwtUtil.extractUsername(token);
                                
                                // Add username to request headers for rate limiter
                                ServerWebExchange mutatedExchange = exchange.mutate()
                                        .request(r -> r.header("X-User-Id", username))
                                        .build();
                                
                                return chain.filter(mutatedExchange);
                            } catch (Exception e) {
                                logger.error("Token validation failed", e);
                                return this.onError(exchange, "Invalid access token", HttpStatus.UNAUTHORIZED);
                            }
                        })
                        .onErrorResume(error -> {
                            logger.error("Error checking token blacklist", error);
                            return this.onError(exchange, "Authentication failed", HttpStatus.UNAUTHORIZED);
                        });
            }
            return chain.filter(exchange);
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        
        // Add CORS headers to error responses
        HttpHeaders headers = response.getHeaders();
        headers.set(org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        headers.set(org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD");
        headers.set(org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*");
        headers.set(org.springframework.http.HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*");
        headers.set(org.springframework.http.HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");
        headers.set(org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "false");
        
        return response.setComplete();
    }

    public static class Config {
        // Put configuration properties here
    }
}
