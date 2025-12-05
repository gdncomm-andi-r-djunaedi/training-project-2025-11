package com.blibli.gatewayModule.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long memberId, String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        return Jwts.builder().subject(String.valueOf(memberId)).claim("email", email)
                .claim("memberId", memberId).issuedAt(now).expiration(expiryDate).signWith(getSigningKey())
                .compact();
    }

    public Claims extractClaims(String token) {
        try {
            return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("Error extracting claims from token: {}", e.getMessage());
            throw new RuntimeException("Invalid or expired token");
        }
    }

    public Long extractMemberId(String token) {
        Claims claims = extractClaims(token);
        return claims.get("memberId", Long.class);
    }

    public boolean validateToken(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

