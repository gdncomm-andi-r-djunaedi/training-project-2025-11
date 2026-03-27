package com.ecom.gateway2.controller.controller;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

import static com.ecom.gateway2.controller.Config.AppConfig.TOKEN_EXPIRATION;

@Component
public class JwtUtil {

    private final SecretKey secretKey;

    @Autowired
    public JwtUtil(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    public String generateToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRATION))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUserId(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (JwtException e) {
            throw new JwtException("Failed to extract user ID from token: " + e.getMessage());
        }
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);

            Claims claims = claimsJws.getBody();
            Date expiration = claims.getExpiration();
            String userId = claims.getSubject();

            return expiration.after(new Date()) && userId != null && !userId.isEmpty();
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}

