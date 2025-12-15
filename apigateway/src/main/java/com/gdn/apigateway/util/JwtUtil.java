package com.gdn.apigateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

@Component
public class JwtUtil {

  @Value("${jwt.secret}")
  private String secret;

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  public Claims validateTokenAndGetClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getSigningKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  public boolean isTokenValid(String token) {
    try {
      validateTokenAndGetClaims(token);
      return true;
    } catch (ExpiredJwtException | MalformedJwtException | 
             UnsupportedJwtException | SignatureException | IllegalArgumentException e) {
      return false;
    }
  }

  public String getUsernameFromToken(String token) {
    return validateTokenAndGetClaims(token).getSubject();
  }

  public String getMemberIdFromToken(String token) {
    return validateTokenAndGetClaims(token).getId();
  }

  /**
   * Get remaining time until token expires
   */
  public Duration getRemainingTime(String token) {
    Claims claims = validateTokenAndGetClaims(token);
    Date expiration = claims.getExpiration();
    long remainingMillis = expiration.getTime() - System.currentTimeMillis();
    return Duration.ofMillis(Math.max(remainingMillis, 0));
  }
}



