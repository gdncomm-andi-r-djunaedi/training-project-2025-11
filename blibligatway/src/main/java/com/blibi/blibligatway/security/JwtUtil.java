package com.blibi.blibligatway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.refresh-expiration:604800000}") // Default: 7 days in milliseconds
    private long refreshExpiration;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }


    public String generateToken(String userId, String email, List<String> roles) {
        Claims claims = Jwts.claims().setSubject(userId);
        claims.put("user_id", userId);
        claims.put("email", email);
        claims.put("roles", roles);

        long now = System.currentTimeMillis();

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Overloaded method for backward compatibility (if needed)
    public String generateToken(Long userId, List<String> roles) {
        return generateToken(String.valueOf(userId), null, roles);
    }

    /**
     * Generate a refresh token (longer expiration, minimal claims)
     * @param userId User ID
     * @return Refresh token string
     */
    public String generateRefreshToken(String userId) {
        Claims claims = Jwts.claims().setSubject(userId);
        claims.put("type", "refresh"); // Mark as refresh token
        claims.put("user_id", userId);

        long now = System.currentTimeMillis();

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + refreshExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Check if token is a refresh token
     * @param token Token to check
     * @return true if it's a refresh token
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return "refresh".equals(claims.get("type"));
        } catch (Exception e) {
            return false;
        }
    }

    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    }

    public Boolean validateToken(String token) {
        try {
            return !getAllClaimsFromToken(token).getExpiration().before(new Date());
        } catch (Exception e) {

            return false;
        }
    }

    public long getRefreshExpiration() {
        return refreshExpiration;
    }
}
