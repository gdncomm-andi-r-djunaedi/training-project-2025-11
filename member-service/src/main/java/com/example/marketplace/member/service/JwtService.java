package com.example.marketplace.member.service;

import com.example.marketplace.common.dto.JwtPayloadDTO;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    @Value("${app.jwt.secret:verysecretverysecretverysecret1234567890}")
    private String secret;

    @Value("${app.jwt.expiration-seconds:3600}")
    private long expirationSeconds;

    private Key key;

    private synchronized void init() {
        if (key == null) {
            key = Keys.hmacShaKeyFor(secret.getBytes());
        }
    }

    public String generateToken(JwtPayloadDTO payload) {
        init();
        Instant now = Instant.now();
        Date iat = Date.from(now);
        Date exp = Date.from(now.plusSeconds(expirationSeconds));

        return Jwts.builder()
                .setSubject(payload.getUserId())
                .claim("username", payload.getUsername())
                .setIssuedAt(iat)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        init();
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }
}
