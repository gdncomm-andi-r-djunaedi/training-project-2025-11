package org.jovian.member.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.jovian.member.entity.Member;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Member member) {

        Date now = new Date();
        Date expiry = new Date(now.getTime() + 86400000); // 1 day

        return Jwts.builder()
                .setIssuedAt(now)
                .setExpiration(expiry)
                .claim("userId", member.getId())         // REQUIRED by gateway
                .claim("email", member.getEmail())       // REQUIRED by gateway
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}
