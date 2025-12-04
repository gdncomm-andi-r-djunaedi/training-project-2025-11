package com.gdn.training.apigateway.infrastructure.security;

import com.gdn.training.apigateway.application.port.JwtTokenPort;
import com.gdn.training.apigateway.application.usecase.model.TokenData;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Infrastructure service that creates HMAC-signed JWT tokens.
 * Uses configured secret (application property: security.jwt.hmac.secret).
 */
@Component
public class JwtTokenService implements JwtTokenPort {
    private final SecretKey secretKey;

    public JwtTokenService(@Value("${security.jwt.hmac.secret:default-secret-insecure-please-change}") String secret) {
        // reuse HMAC key creation; ensure key length is sufficient for HS256/HS512
        // usage
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    @Override
    public TokenData createToken(String subject, Map<String, Object> claims, long expiration) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expiration);
        String jti = UUID.randomUUID().toString();

        // add jti to claims map
        io.jsonwebtoken.JwtBuilder builder = Jwts.builder()
                .setId(jti)
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .addClaims(claims)
                .signWith(secretKey, SignatureAlgorithm.HS256);

        // sign token
        String token = builder.compact();

        return new TokenData(token, jti, expiration);
    }
}
