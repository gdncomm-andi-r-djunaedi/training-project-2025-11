package com.example.apigateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String secret = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437"; // Example 256-bit
                                                                                                      // secret

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", secret);
    }

    @Test
    void createToken_shouldReturnSignedToken() {
        String token = jwtUtil.createToken("user123");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void validateToken_shouldReturnClaims_whenTokenIsValid() {
        String token = jwtUtil.createToken("user123");
        Claims claims = jwtUtil.validateToken(token);
        assertEquals("user123", claims.getSubject());
        assertEquals("user123", claims.get("user_id"));
    }

    @Test
    void extractToken_shouldExtractFromBearerHeader() {
        String token = jwtUtil.createToken("user123");
        ServerHttpRequest request = MockServerHttpRequest.get("/")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();

        Optional<String> extracted = jwtUtil.extractToken(request);
        assertTrue(extracted.isPresent());
        assertEquals(token, extracted.get());
    }

    @Test
    void extractToken_shouldIgnoreInvalidHeaderFormat() {
        String token = jwtUtil.createToken("user123");
        ServerHttpRequest request = MockServerHttpRequest.get("/")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + token) // Not Bearer
                .build();

        Optional<String> extracted = jwtUtil.extractToken(request);
        assertFalse(extracted.isPresent());
    }

    @Test
    void extractToken_shouldExtractFromCookie_whenHeaderMissing() {
        String token = jwtUtil.createToken("user123");
        ServerHttpRequest request = MockServerHttpRequest.get("/")
                .cookie(new HttpCookie("jwt", token))
                .build();

        Optional<String> extracted = jwtUtil.extractToken(request);
        assertTrue(extracted.isPresent());
        assertEquals(token, extracted.get());
    }

    @Test
    void extractToken_shouldReturnEmpty_whenBothMissing() {
        ServerHttpRequest request = MockServerHttpRequest.get("/").build();
        Optional<String> extracted = jwtUtil.extractToken(request);
        assertFalse(extracted.isPresent());
    }

    @Test
    void createCookie_shouldReturnSecureCookie() {
        String token = "sample-token";
        ResponseCookie cookie = jwtUtil.createCookie(token);

        assertEquals("jwt", cookie.getName());
        assertEquals("sample-token", cookie.getValue());
        assertTrue(cookie.isHttpOnly());
        assertTrue(cookie.isSecure());
        assertEquals("/", cookie.getPath());
        assertEquals(30 * 60, cookie.getMaxAge().getSeconds());
    }

    @Test
    void clearCookie_shouldReturnExpiredCookie() {
        ResponseCookie cookie = jwtUtil.clearCookie();

        assertEquals("jwt", cookie.getName());
        assertEquals("", cookie.getValue());
        assertEquals(0, cookie.getMaxAge().getSeconds());
    }
}
