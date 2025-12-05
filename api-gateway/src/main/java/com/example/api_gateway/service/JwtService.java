package com.example.api_gateway.service;

import com.example.api_gateway.exception.TokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private String SECRET_KEY = "my_secret_key_123456";

    public String generateToken(String username, UUID userId,String email) {
        Key key = new SecretKeySpec(SECRET_KEY.getBytes(), SignatureAlgorithm.HS256.getJcaName());
        return Jwts.builder()
                .claim("userId", userId)
                .claim("username", username)
                .claim("email",email)
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 4))
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();
    }

    public Claims validateToken(String token) throws Exception {
        if(token==null || token.isEmpty()){
            throw new TokenException("Token should not be null or empty");
        }
        try {
            Key key = new SecretKeySpec(SECRET_KEY.getBytes(), SignatureAlgorithm.HS256.getJcaName());
            Claims claims = Jwts.parser()
                    .setSigningKey(key)
                    .parseClaimsJws(token)
                    .getBody();
            return claims;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new TokenException("Token has expired");
        } catch (Exception e) {
            throw new TokenException("Invalid token");
        }
    }

    public String getUsernameFromToken(String token) throws Exception {
        Claims claims = validateToken(token);
        return claims.get("username", String.class);
    }

    public UUID getUserIdFromToken(String token) throws Exception {
        Claims claims = validateToken(token);
        String userIdStr = claims.get("userId", String.class);
        return UUID.fromString(userIdStr);
    }

    public String getUserMailFromToken(String token) throws Exception {
        Claims claims = validateToken(token);
        return claims.get("email", String.class);
    }
}
