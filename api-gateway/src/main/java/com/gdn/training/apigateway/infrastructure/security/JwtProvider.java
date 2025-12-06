package com.gdn.training.apigateway.infrastructure.security;

import java.util.Map;
import java.util.Optional;

public interface JwtProvider {
    boolean validateToken(String token);

    Optional<Map<String, Object>> parseClaims(String token);

    String providerId();
}
