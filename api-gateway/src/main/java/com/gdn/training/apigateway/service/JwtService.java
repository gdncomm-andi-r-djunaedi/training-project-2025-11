package com.gdn.training.apigateway.service;

import io.jsonwebtoken.Claims;
import java.util.function.Function;

public interface JwtService {
    void validateToken(final String token);

    String generateToken(String userName);

    String extractUsername(String token);

    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);

    void invalidateToken(String token);

    boolean isTokenInvalid(String token);
}
