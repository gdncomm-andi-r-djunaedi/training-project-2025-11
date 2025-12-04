package com.marketplace.gateway.filter;

import com.marketplace.common.util.JwtUtil;
import com.marketplace.gateway.constant.GatewayConstants;
import com.marketplace.gateway.exception.InvalidTokenException;
import com.marketplace.gateway.util.CookieUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.UUID;

/**
 * JWT Authentication Filter for API Gateway
 * Validates JWT tokens from Cookie OR Authorization header
 * Adds user information to headers for downstream services
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, CookieUtil cookieUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
        this.cookieUtil = cookieUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            log.debug("JWT Authentication Filter executing for: {}", exchange.getRequest().getPath());

            String token = extractToken(exchange);

            if (token == null) {
                log.warn("Missing authorization token");
                throw InvalidTokenException.missing();
            }

            try {
                if (!jwtUtil.validateToken(token)) {
                    log.warn("Invalid JWT token");
                    throw InvalidTokenException.malformed();
                }

                // Extract user information from token
                UUID userId = jwtUtil.extractUserId(token);
                String username = jwtUtil.extractUsername(token);

                log.debug("Authenticated user: {} (ID: {})", username, userId);

                // Add user information to request headers for downstream services
                ServerWebExchange modifiedExchange = exchange.mutate()
                        .request(r -> r
                                .header(GatewayConstants.Headers.USER_ID, userId.toString())
                                .header(GatewayConstants.Headers.USERNAME, username))
                        .build();

                return chain.filter(modifiedExchange);

            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                log.warn("JWT token expired: {}", e.getMessage());
                throw InvalidTokenException.expired();
            } catch (Exception e) {
                log.error("JWT validation error: {}", e.getMessage(), e);
                throw InvalidTokenException.malformed();
            }
        };
    }

    /**
     * Extract JWT token from Cookie (priority) or Authorization header
     */
    private String extractToken(ServerWebExchange exchange) {
        // 1. Try to extract from Cookie first (more secure)
        HttpCookie cookie = exchange.getRequest().getCookies().getFirst(cookieUtil.getAuthCookieName());
        if (cookie != null && cookie.getValue() != null && !cookie.getValue().isEmpty()) {
            log.debug("Token extracted from cookie");
            return cookie.getValue();
        }

        // 2. Fall back to Authorization header
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            log.debug("Token extracted from Authorization header");
            return authHeader.substring(7);
        }

        return null;
    }

    public static class Config {
        // Configuration properties if needed
    }
}
