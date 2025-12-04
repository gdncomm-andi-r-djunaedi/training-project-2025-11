package com.gdn.training.apigateway.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class HmacJwtProvider implements JwtProvider {

    private final Key key;

    public HmacJwtProvider(
            @Value("${security.jwt.hmac.secret:default-secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    @Override
    public Optional<Map<String, Object>> parseClaims(String token) {
        try {
            return Optional.of(Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public String providerId() {
        return "hmac";
    }
}
