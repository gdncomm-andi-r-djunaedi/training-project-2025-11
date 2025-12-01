package com.example.common.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

public class JwtService {

  private final SecretKey key;

  public JwtService(String secret) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  public String generateToken(String subject, Map<String, Object> claims, long ttlSeconds) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(subject)
        .claims(claims)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(ttlSeconds)))
        .signWith(key)
        .compact();
  }

  public String validateAndGetSubject(String token) {
    return Jwts.parser()             // 0.12.x API
        .verifyWith(key)            // SecretKey overload
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .getSubject();
  }
}