package org.jovian.apigateway.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.beans.factory.annotation.Value;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    // Extract JWT token from Authorization header or cookie
    public String extractToken(ServerWebExchange exchange) {
        // 1. Check Authorization header
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // 2. Check cookie
        if (exchange.getRequest().getCookies().containsKey("jwt")) {
            return exchange.getRequest().getCookies().getFirst("jwt").getValue();
        }

        return null;
    }

    // Validate JWT signature and expiration
    public boolean isTokenValid(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Extract userId claim
    public String extractUserId(String token) {
        return extractAllClaims(token).get("userId", String.class);
    }

    // Extract email claim
    public String extractEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    // Extract expiration in seconds (for blacklist)
    public long getExpirationInSeconds(String token) {
        Claims claims = extractAllClaims(token);
        long now = System.currentTimeMillis();
        long expMillis = claims.getExpiration().getTime();
        return (expMillis - now) / 1000;
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    // Return 401 unauthorized response
    public Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
