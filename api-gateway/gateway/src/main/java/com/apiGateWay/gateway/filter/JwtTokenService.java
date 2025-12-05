package com.apiGateWay.gateway.filter;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.security.PublicKey;

import java.util.Date;

@Slf4j
@Component
public class JwtTokenService {

    private final PublicKey publicKey;
    private final PrivateKey privateKey;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.access-token.expiration:3600000}") // Default: 1 hour
    private long accessTokenExpirationMs;

    @Value("${jwt.issuer:gateway-service}")
    private String issuer;

    public JwtTokenService(PublicKey publicKey, PrivateKey privateKey,
            RedisTemplate<String, String> redisTemplate) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.redisTemplate = redisTemplate;
    }

    public String generateAccessToken(String email) {
        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + accessTokenExpirationMs);
            String token = Jwts.builder()
                    .setSubject(email)
                    .setIssuer(issuer)
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .claim("type", "access")
                    .signWith(privateKey, SignatureAlgorithm.RS256)
                    .compact();
            log.info("Access token generated successfully for user: {}", email);
            return token;
        } catch (Exception e) {
            log.error("Error generating access token for user: {}", email, e);
            throw new RuntimeException("Failed to generate access token", e);
        }
    }

    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("Token is null or empty");
            return false;
        }
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            if (isTokenBlacklisted(token)) {
                log.warn("Token is blacklisted");
                return false;
            }
            String tokenType = claims.get("type", String.class);
            if (!"access".equals(tokenType)) {
                log.warn("Token type is not 'access': {}", tokenType);
                return false;
            }
            Date expiration = claims.getExpiration();
            if (expiration.before(new Date())) {
                log.warn("Token has expired");
                return false;
            }
            log.debug("Token validated successfully for user: {}", claims.getSubject());
            return true;

        } catch (ExpiredJwtException e) {
            log.warn("Token has expired: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.error("Malformed JWT token: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            log.error("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Error extracting email from token: {}", e.getMessage());
            throw new RuntimeException("Invalid token", e);
        }
    }

    public String extractTokenFromHeader(String authHeader) {
        if (authHeader == null || authHeader.trim().isEmpty()) {
            return null;
        }
        if (authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return authHeader;
    }

    public ValidationResult validateWithContext(String token) {
        if (!validateToken(token)) {
            return new ValidationResult(false, null);
        }
        try {
            String email = getEmailFromToken(token);
            return new ValidationResult(true, email);
        } catch (Exception e) {
            log.error("Error extracting context from token: {}", e.getMessage());
            return new ValidationResult(false, null);
        }
    }

    public boolean isTokenBlacklisted(String token) {
        try {
            String redisKey = "blacklist_token:" + token;
            Boolean exists = redisTemplate.hasKey(redisKey);
            return exists != null && exists;
        } catch (Exception e) {
            log.error("Error checking token blacklist: {}", e.getMessage());
            return false;
        }
    }

    public static class ValidationResult {
        private final boolean valid;
        private final String email;

        public ValidationResult(boolean valid, String email) {
            this.valid = valid;
            this.email = email;
        }

        public boolean isValid() {
            return valid;
        }

        public String getEmail() {
            return email;
        }
    }
}
