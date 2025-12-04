package com.blublu.api_gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {
  private final Key SIGNING_KEY;
  private final Long TOKEN_VALIDITY;

  public JwtUtil(@Value("${jwt.secret}") String secret, @Value("${jwt.expiryInMinutes}") String expiry) {
    this.SIGNING_KEY = Keys.hmacShaKeyFor(secret.getBytes());
    this.TOKEN_VALIDITY = Long.parseLong(expiry);
  }

  public String generateToken(String username) {
    Map<String, Object> claims = new HashMap<>();
    return createToken(claims, username);
  }

  private String createToken(Map<String, Object> claims, String subject) {
    long now = System.currentTimeMillis();
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(subject)
        .setIssuedAt(new Date(now))
        .setExpiration(Date.from(Instant.now().plus(TOKEN_VALIDITY, ChronoUnit.MINUTES)))
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
    try {
      return Jwts.parserBuilder().setSigningKey(SIGNING_KEY).build().parseClaimsJws(token).getBody();
    } catch (SignatureException signatureException) {
      throw new SignatureException("Unauthorize");
    }
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

  public String getTokenFromAuthHeaders(ServerHttpRequest request) {
    String authHeader = request.getHeaders().getFirst("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }
    return null;
  }
}
