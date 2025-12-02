package com.blublu.api_gateway.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {
  private final Key SIGNING_KEY;

  public JwtUtil(@Value("${jwt.secret}") String secret) {
    this.SIGNING_KEY = Keys.hmacShaKeyFor(secret.getBytes());
  }

  public String generateToken(String username) {
    Map<String, Object> claims = new HashMap<>();
    return createToken(claims, username);
  }

  private String createToken(Map<String, Object> claims, String subject) {
    long now = System.currentTimeMillis();
    final long TOKEN_VALIDITY = 60 * 60 * 1000;
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(subject)
        .setIssuedAt(new Date(now))
        .setExpiration(new Date(now + TOKEN_VALIDITY))
        .signWith(SIGNING_KEY)
        .compact();
  }

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parserBuilder().setSigningKey(SIGNING_KEY).build().parseClaimsJws(token).getBody();
  }

  public boolean isTokenValid(String token, String usernameRequest) {
    // Need to add redis checking here
    final String username = extractUsername(token);

    return (username.equals(usernameRequest) && !isTokenExpired(token));
  }

  // Check expiration
  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }
}
