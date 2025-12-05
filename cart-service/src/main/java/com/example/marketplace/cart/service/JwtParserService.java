package com.example.marketplace.cart.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;

@Service
public class JwtParserService {

    @Value("${app.jwt.secret:verysecretverysecretverysecret1234567890}")
    private String secret;

    private Key key;

    private synchronized void init() {
        if (key == null) {
            key = Keys.hmacShaKeyFor(secret.getBytes());
        }
    }

    public String extractUserId(String token) {
        init();
        Jws<Claims> jws = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
        return jws.getBody().getSubject();
    }
}
