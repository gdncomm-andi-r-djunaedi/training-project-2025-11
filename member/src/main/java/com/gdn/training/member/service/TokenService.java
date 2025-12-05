package com.gdn.training.member.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class TokenService {

    private final String secret;

    public TokenService() {
        this.secret = "testkey";
    }

    public String generateToken(String username) {
        // Create JWT (Signed with HMAC SHA256)
        return JWT.create()
                .withSubject(username)
                .withIssuer("testingIssuer")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(new Date().getTime() + 60 * 60 * 1000)) // 1 hour
                .withJWTId(UUID.randomUUID().toString())
                .sign(Algorithm.HMAC256(secret));
    }
}
