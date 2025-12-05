package com.kailash.apigateway.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    public String generateToken(String userId, Map<String, Object> claims, long expirySeconds) {
        Algorithm algo = Algorithm.HMAC256(secret);

        return JWT.create()
                .withSubject(userId)
                .withExpiresAt(new Date(System.currentTimeMillis() + expirySeconds * 1000))
                .withIssuedAt(new Date())
                .withPayload(claims)
                .sign(algo);
    }

    public DecodedJWT validate(String token) {

        Algorithm algo = Algorithm.HMAC256(secret);
        return JWT.require(algo).build().verify(token);
    }
}
