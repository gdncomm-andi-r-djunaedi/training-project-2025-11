package com.blibli.apigateway.service;

import com.blibli.apigateway.config.JwtProperties;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class JwtService {
    private static JwtProperties jwtProperties = new JwtProperties();
    private final TokenBlacklistService tokenBlacklistService;
    
    public JwtService(JwtProperties jwtProperties, TokenBlacklistService tokenBlacklistService) {
        JwtService.jwtProperties = jwtProperties;
        this.tokenBlacklistService = tokenBlacklistService;
    }
    
    public String generateToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        
        return createToken(claims, email);
    }
    
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpiration());
        
        SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
        
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }
    
    public static String extractEmail(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
            
            var claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            String email = (String) claims.get("email");
            if (email == null) {
                email = claims.getSubject();
            }
            
            return email;
        } catch (Exception e) {
            throw new RuntimeException("Invalid or expired token", e);
        }
    }
    
    public boolean validateToken(String token) {
        try {
            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                log.warn("Token is blacklisted (logged out)");
                return false;
            }
            
            log.debug("Validating token. Token length: {}, JWT Secret configured: {}", 
                        token.length(), jwtProperties.getSecret() != null ? "Yes" : "No");
            
            SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
            
            var claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            log.debug("Token validated successfully. Subject: {}, Email: {}, Issued at: {}, Expires at: {}", 
                        claims.getSubject(), claims.get("email"), claims.getIssuedAt(), claims.getExpiration());
            
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token expired. Expired at: {}, Current time: {}", e.getClaims().getExpiration(), new Date());
            return false;
        } catch (SignatureException e) {
            log.error("Invalid token signature. This usually means the JWT secret doesn't match. Error: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.error("Malformed token. Error: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Token validation error. Error Type: {}, Error Message: {}", 
                        e.getClass().getSimpleName(), e.getMessage(), e);
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
            Jwts.parser()
                    .clockSkewSeconds(0)
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return false;
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            try {
                SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
                var claims = Jwts.parser()
                        .clockSkewSeconds(Long.MAX_VALUE)
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
                Date expiration = claims.getExpiration();
                if (expiration != null && expiration.before(new Date())) {
                    return true;
                }
            } catch (Exception e2) {
            }
            return false;
        }
    }

    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklistService.isTokenBlacklisted(token);
    }
}

