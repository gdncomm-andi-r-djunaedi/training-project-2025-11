package com.gdn.training.api_gateway.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "ZmFrZXNlY3JldGtleWZha2VzZWNyZXRrZXkzMjMyMzIzMjMyMzIzMjMy";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 10);
    }

    @Test
    void generateTokenEmbedsClaimsAndSubject() {
        String token = jwtService.generateToken("123", Map.of("email", "user@example.com", "role", "ROLE_USER"));

        Claims claims = jwtService.parseToken(token);

        assertThat(claims.getSubject()).isEqualTo("123");
        assertThat(claims.get("email", String.class)).isEqualTo("user@example.com");
        assertThat(claims.get("role", String.class)).isEqualTo("ROLE_USER");
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    void remainingTtlNeverReturnsNegative() {
        String token = jwtService.generateToken("123", Map.of());
        Claims claims = jwtService.parseToken(token);

        assertThat(jwtService.remainingTtl(claims))
                .isLessThanOrEqualTo(Duration.ofMinutes(10))
                .isGreaterThan(Duration.ZERO);
    }
}

