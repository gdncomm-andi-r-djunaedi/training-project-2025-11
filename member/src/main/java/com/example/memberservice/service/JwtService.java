package com.example.memberservice.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    /**
     * Generate a JWT token for the given username.
     * 
     * @param username the username to include in the token
     * @return signed JWT string
     */
    public String generateToken(String username, String userId) {
        long now = System.currentTimeMillis();
        long expiry = now + 30 * 60 * 1000; // 30 minutes
        return Jwts.builder()
                .setSubject(userId)
                .claim("username", username)
                .claim("user_id", userId)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(expiry))
                .signWith(getSignKey())
                .compact();
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
