package com.marketplace.common.security;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class JwtTokenProvider {

    private final RsaKeyProperties rsaKeyProperties;
    private final long accessTokenValidity;
    private final long refreshTokenValidity;

    public JwtTokenProvider(
            RsaKeyProperties rsaKeyProperties,
            @Value("${jwt.access-token-validity:3600000}") long accessTokenValidity,
            @Value("${jwt.refresh-token-validity:86400000}") long refreshTokenValidity) {
        this.rsaKeyProperties = rsaKeyProperties;
        this.accessTokenValidity = accessTokenValidity;
        this.refreshTokenValidity = refreshTokenValidity;
    }

    public String generateAccessToken(UUID memberId, String email) {
        if (!rsaKeyProperties.hasPrivateKey()) {
            throw new IllegalStateException("Cannot generate tokens without private key");
        }
        return generateToken(memberId, email, accessTokenValidity, "ACCESS");
    }

    public String generateRefreshToken(UUID memberId, String email) {
        if (!rsaKeyProperties.hasPrivateKey()) {
            throw new IllegalStateException("Cannot generate tokens without private key");
        }
        return generateToken(memberId, email, refreshTokenValidity, "REFRESH");
    }

    private String generateToken(UUID memberId, String email, long validity, String type) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + validity);

        Map<String, Object> claims = new HashMap<>();
        claims.put("memberId", memberId.toString());
        claims.put("email", email);
        claims.put("type", type);

        return Jwts.builder()
                .claims(claims)
                .subject(memberId.toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(rsaKeyProperties.getPrivateKey(), Jwts.SIG.RS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(rsaKeyProperties.getPublicKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        } catch (Exception ex) {
            log.error("JWT validation error: {}", ex.getMessage());
        }
        return false;
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(rsaKeyProperties.getPublicKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID getMemberIdFromToken(String token) {
        Claims claims = getClaims(token);
        String memberId = claims.get("memberId", String.class);
        return UUID.fromString(memberId);
    }

    public String getEmailFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("email", String.class);
    }

    public String getTokenType(String token) {
        Claims claims = getClaims(token);
        return claims.get("type", String.class);
    }

    public Date getExpirationFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.getExpiration();
    }

    public long getRemainingValidityInMillis(String token) {
        Date expiration = getExpirationFromToken(token);
        return expiration.getTime() - System.currentTimeMillis();
    }

    public long getAccessTokenValidity() {
        return accessTokenValidity;
    }
}
