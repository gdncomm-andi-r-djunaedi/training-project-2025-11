package com.example.apigateway.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

  private final Key signingKey;
  private final long expirationMs;


  public JwtUtil(
          @Value("${jwt.secret}") String secret,
          @Value("${jwt.expiration-ms:3600000}") long expirationMs
  ) {
    this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expirationMs = expirationMs;
  }


  @SuppressWarnings("unchecked")
  public String extractUserId(String token) {
    Claims claims = getClaims(token);
    Map<String, String> userClaims = (Map<String, String>) claims.get("claims", Map.class);
    return userClaims.get("id");
  }

  public boolean validate(String token) {
    try {
      getClaims(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  private Claims getClaims(String token) {
    return Jwts.parserBuilder()
            .setSigningKey(signingKey)
            .build()
            .parseClaimsJws(token)
            .getBody();
  }

  public String generateToken(String userId, Map<String, Object> extraClaims) {
    long now = System.currentTimeMillis();
    Date issuedAt = new Date(now);
    Date expiry = new Date(now + expirationMs);

    JwtBuilder builder = Jwts.builder()
            .setSubject(userId)
            .setIssuedAt(issuedAt)
            .setExpiration(expiry)
            .addClaims(extraClaims)
            .signWith(signingKey, SignatureAlgorithm.HS256);

    return builder.compact();
  }
}