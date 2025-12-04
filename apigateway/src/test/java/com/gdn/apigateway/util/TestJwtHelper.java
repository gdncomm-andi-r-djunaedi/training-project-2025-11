package com.gdn.apigateway.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Helper class for generating JWT tokens in tests
 */
public class TestJwtHelper {

  // Must match the test application.properties jwt.secret
  private static final String SECRET = "testsecretkey123456789012345678901234567890";
  private static final long EXPIRATION_MS = 3600000; // 1 hour

  private static SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Generate a valid JWT token
   */
  public static String generateToken(String memberId, String username) {
    return Jwts.builder()
        .setId(memberId)
        .setSubject(username)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  /**
   * Generate an expired JWT token
   */
  public static String generateExpiredToken(String memberId, String username) {
    return Jwts.builder()
        .setId(memberId)
        .setSubject(username)
        .setIssuedAt(new Date(System.currentTimeMillis() - 7200000)) // 2 hours ago
        .setExpiration(new Date(System.currentTimeMillis() - 3600000)) // 1 hour ago
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  /**
   * Generate a token with different secret (invalid signature)
   */
  public static String generateInvalidSignatureToken(String memberId, String username) {
    SecretKey wrongKey = Keys.hmacShaKeyFor("wrongsecretkey12345678901234567890123".getBytes(StandardCharsets.UTF_8));
    return Jwts.builder()
        .setId(memberId)
        .setSubject(username)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
        .signWith(wrongKey, SignatureAlgorithm.HS256)
        .compact();
  }

  /**
   * Generate a token that expires soon (for testing remaining time)
   */
  public static String generateShortLivedToken(String memberId, String username, long expirationMs) {
    return Jwts.builder()
        .setId(memberId)
        .setSubject(username)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
  }
}

