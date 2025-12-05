package com.blublu.api_gateway.util;

import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String secret = "mysecretkeymustbelongenoughforhmacsha256"; // Must be at least 32 chars
    private final String expiry = "60";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(secret, expiry);
    }

    @Test
    void testGenerateToken() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testExtractUsername() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        String extractedUsername = jwtUtil.extractUsername(token);
        assertEquals(username, extractedUsername);
    }

    @Test
    void testIsTokenValid() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        assertTrue(jwtUtil.isTokenValid(token, username));
    }

    @Test
    void testIsTokenValid_InvalidUsername() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        assertFalse(jwtUtil.isTokenValid(token, "otheruser"));
    }

    @Test
    void testExtractExpiration() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        Date expiration = jwtUtil.extractExpiration(token);
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void testGetTokenFromAuthHeaders_ValidHeader() {
        String token = "valid-token";
        ServerHttpRequest request = MockServerHttpRequest.get("/")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();

        String extractedToken = jwtUtil.getTokenFromAuthHeaders(request);
        assertEquals(token, extractedToken);
    }

    @Test
    void testGetTokenFromAuthHeaders_NoHeader() {
        ServerHttpRequest request = MockServerHttpRequest.get("/")
                .build();

        String extractedToken = jwtUtil.getTokenFromAuthHeaders(request);
        assertNull(extractedToken);
    }

    @Test
    void testGetTokenFromAuthHeaders_InvalidHeaderFormat() {
        ServerHttpRequest request = MockServerHttpRequest.get("/")
                .header(HttpHeaders.AUTHORIZATION, "Basic some-auth")
                .build();

        String extractedToken = jwtUtil.getTokenFromAuthHeaders(request);
        assertNull(extractedToken);
    }

    @Test
    void testExtractAllClaims_InvalidSignature() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        // Tamper with the token
        String tamperedToken = token + "tampered";

        assertThrows(Exception.class, () -> jwtUtil.extractUsername(tamperedToken));
    }
}
