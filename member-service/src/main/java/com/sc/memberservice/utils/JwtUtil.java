package com.sc.memberservice.utils;

import com.sc.memberservice.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    public String extractEmail(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (JwtException | InvalidTokenException e) {
            throw new InvalidTokenException("Invalid or expired token", e);
        }
    }

    public Date extractExpiration(String token) {
        try {
            return extractClaim(token, Claims::getExpiration);
        } catch (JwtException | InvalidTokenException e) {
            throw new InvalidTokenException("Invalid or expired token", e);
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        } catch (JwtException | InvalidTokenException e) {
            throw new InvalidTokenException("Invalid or expired token", e);
        }
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts
                    .parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            throw new InvalidTokenException("Invalid or expired token", e);
        }
    }

    public String extractSubject(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (JwtException e) {
            throw new InvalidTokenException("Invalid or expired token", e);
        }
    }

    public Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (JwtException | InvalidTokenException e) {
            throw new InvalidTokenException("Invalid or expired token", e);
        }
    }

    public Boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String email = extractEmail(token);
            return (email.equals(userDetails.getUsername())) && !isTokenExpired(token);
        } catch (JwtException | InvalidTokenException e) {
            throw new InvalidTokenException("Invalid or expired token", e);
        }
    }

    public String generateToken(String idAndEmail) {
        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("email", idAndEmail.split(":")[1]);
            return createToken(claims, idAndEmail);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate token", e);
        }
    }

    private String createToken(Map<String, Object> claims, String subject) {
        try {
            return Jwts.builder()
                    .claims(claims)
                    .subject(subject)
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + 20000))
                    .signWith(getSecretKey())
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create token", e);
        }
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(
                "HiThisIsMyTrainingProject2025ForTransitionFromQaToDevelopment"
        ));
    }
}
