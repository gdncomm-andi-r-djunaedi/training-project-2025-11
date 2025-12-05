package com.gdn.member.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static com.gdn.member.utils.Constants.SECRET_KEY;


@Service
public class JwtGenerator {

    public String generateToken(String username) {
        Instant now = Instant.now();
        Date expirationDate = Date.from(now.plus(1, ChronoUnit.HOURS));

        String jwtToken = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(Date.from(now))
                .setExpiration(expirationDate)
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
        return jwtToken;
    }
}
