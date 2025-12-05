package com.zasura.apiGateway.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;

@Service
public class JwtService {
  @Value("${jwt.secret.key}")
  private String secretKey;

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
  }

  public String generateToken(String subject) {
    Instant issuedAt = Instant.now();
    Instant expiration = issuedAt.plus(10, ChronoUnit.MINUTES);

    String token = Jwts.builder()
        .subject(subject)
        .claims()
        .add(new HashMap<>())
        .issuedAt(Date.from(issuedAt))
        .expiration(Date.from(expiration))
        .and()
        .signWith(getSigningKey())
        .compact();

    return token;
  }

  public Mono<Boolean> validateToken(String token) {
    return Mono.fromCallable(() -> !isTokenExpired(token))
        .onErrorResume(ExpiredJwtException.class, e -> Mono.just(false))
        .onErrorResume(Exception.class, e -> Mono.just(false));
  }

  public Claims extractAllClaims(String token) {
    return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
    final Claims claims =
        Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
    return claimResolver.apply(claims);
  }

  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

}
