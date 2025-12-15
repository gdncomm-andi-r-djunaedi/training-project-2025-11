package com.training.marketplace.member.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtUtils {
    @Value("${app.jwt.secret}")
    private String jwtSecret;
    @Value("${app.jwt.expiration}")
    private long jwtExpirationMs;
    @Value("${app.jwt.refresh-expiration}")
    private long jwtRefreshExpirationMs;

    private static final String TOKEN_PREFIX = "Bearer ";

    public String generateAccessToken(Authentication authentication){
        return generateToken(authentication, jwtExpirationMs, "access");
    }

    public String generateRefreshToken(Authentication authentication){
        return generateToken(authentication, jwtExpirationMs, "refresh");

    }

    public boolean isValidToken(String token, UserDetails userDetails){
        final String username = extractUsernameFromToken(token);

        if(!username.equals(userDetails.getUsername())) return false;

        try{
            Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SignatureException e){
            log.error(e.getMessage());
        } catch (MalformedJwtException e) {
            log.error(e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error(e.getMessage());
        } catch (UnsupportedJwtException e){
            log.error(e.getMessage());
        } catch (IllegalArgumentException e){
            log.error(e.getMessage());
        }
        return false;
    }

    private SecretKey getSignInKey(){
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String generateToken(Authentication authentication, long expireDuration, String tokenType){
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Date now = new Date();
        Date expiration = new Date(now.getTime() + expireDuration);

        Map<String, String> claims = new HashMap<>();
        claims.put("token_type",tokenType);

        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .claims(claims)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSignInKey())
                .compact();
    }

    public String extractUsernameFromToken(String token){
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean isRefreshToken(String token, UserDetails userDetails){
        Claims claims = Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("token_type").equals("refresh");
    }
}
