package com.example.apigateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    /**
     * Validate a JWT token and return its claims. Throws if invalid/expired.
     */
    public Claims validateToken(final String token) {
        return Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token).getBody();
    }

    /**
     * Create a JWT token with the required payload.
     * 
     * @param userId the user identifier
     * @return signed JWT string
     */
    public String createToken(String userId) {
        long now = System.currentTimeMillis();
        long expiry = now + 30 * 60 * 1000; // 30 minutes
        return Jwts.builder()
                .setSubject(userId)
                .claim("user_id", userId)
                .setIssuedAt(new java.util.Date(now))
                .setExpiration(new java.util.Date(expiry))
                .signWith(getSignKey())
                .compact();
    }

    /**
     * Extract JWT token from the request. Checks the Authorization header first,
     * then the "jwt" cookie.
     */
    public java.util.Optional<String> extractToken(org.springframework.http.server.reactive.ServerHttpRequest request) {
        // Header check
        java.util.List<String> authHeaders = request.getHeaders()
                .getOrEmpty(org.springframework.http.HttpHeaders.AUTHORIZATION);
        if (!authHeaders.isEmpty()) {
            String bearer = authHeaders.get(0);
            if (bearer != null && bearer.startsWith("Bearer ")) {
                return java.util.Optional.of(bearer.substring(7));
            }
        }
        // Cookie check
        if (request.getCookies().containsKey("jwt")) {
            return java.util.Optional.of(request.getCookies().getFirst("jwt").getValue());
        }
        return java.util.Optional.empty();
    }

    /**
     * Create a secure cookie containing the JWT.
     */
    public org.springframework.http.ResponseCookie createCookie(String token) {
        return org.springframework.http.ResponseCookie.from("jwt", token)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(java.time.Duration.ofMinutes(30))
                .build();
    }

    /**
     * Invalidate the JWT cookie by setting max age to zero.
     */
    public org.springframework.http.ResponseCookie clearCookie() {
        return org.springframework.http.ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(java.time.Duration.ZERO)
                .build();
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
