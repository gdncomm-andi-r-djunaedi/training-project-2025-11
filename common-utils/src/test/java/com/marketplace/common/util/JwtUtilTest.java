package com.marketplace.common.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    private static final String TEST_SECRET = "ThisIsAVeryLongSecretKeyForTestingPurposesThatIsAtLeast256BitsLong";
    private static final Long TEST_EXPIRATION = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", TEST_EXPIRATION);
    }

    @Test
    void generateToken_WithEmail_Success() {
        String email = "test@example.com";

        String token = jwtUtil.generateToken(email);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(email, jwtUtil.extractEmail(token));
    }

    @Test
    void generateToken_WithUserIdEmailAndRoles_Success() {
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        List<String> roles = Arrays.asList("ROLE_USER", "ROLE_ADMIN");

        String token = jwtUtil.generateToken(userId, email, roles);

        assertNotNull(token);
        assertEquals(userId, jwtUtil.extractUserId(token));
        assertEquals(email, jwtUtil.extractEmail(token));
        assertEquals(roles, jwtUtil.extractRoles(token));
    }

    @Test
    void generateToken_WithCustomClaims_Success() {
        String email = "test@example.com";
        Map<String, Object> claims = new HashMap<>();
        claims.put("customKey", "customValue");

        String token = jwtUtil.generateToken(email, claims);

        assertNotNull(token);
        assertEquals(email, jwtUtil.extractEmail(token));
    }

    @Test
    void extractUserId_ValidToken_ReturnsUserId() {
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        List<String> roles = Arrays.asList("ROLE_USER");

        String token = jwtUtil.generateToken(userId, email, roles);
        UUID extractedUserId = jwtUtil.extractUserId(token);

        assertEquals(userId, extractedUserId);
    }

    @Test
    void extractEmail_ValidToken_ReturnsEmail() {
        String email = "test@example.com";

        String token = jwtUtil.generateToken(email);
        String extractedEmail = jwtUtil.extractEmail(token);

        assertEquals(email, extractedEmail);
    }

    @Test
    void extractRoles_ValidToken_ReturnsRoles() {
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        List<String> roles = Arrays.asList("ROLE_USER", "ROLE_ADMIN");

        String token = jwtUtil.generateToken(userId, email, roles);
        List<String> extractedRoles = jwtUtil.extractRoles(token);

        assertEquals(roles, extractedRoles);
    }

    @Test
    void extractExpiration_ValidToken_ReturnsFutureDate() {
        String email = "test@example.com";

        String token = jwtUtil.generateToken(email);
        Date expiration = jwtUtil.extractExpiration(token);

        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void validateToken_ValidToken_ReturnsTrue() {
        String email = "test@example.com";

        String token = jwtUtil.generateToken(email);
        Boolean isValid = jwtUtil.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    void validateToken_WithEmail_ValidToken_ReturnsTrue() {
        String email = "test@example.com";

        String token = jwtUtil.generateToken(email);
        Boolean isValid = jwtUtil.validateToken(token, email);

        assertTrue(isValid);
    }

    @Test
    void validateToken_WithEmail_WrongEmail_ReturnsFalse() {
        String email = "test@example.com";
        String wrongEmail = "wrong@example.com";

        String token = jwtUtil.generateToken(email);
        Boolean isValid = jwtUtil.validateToken(token, wrongEmail);

        assertFalse(isValid);
    }

    @Test
    void validateToken_ExpiredToken_ReturnsFalse() {
        // Set a very short expiration
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1000L);
        String email = "test@example.com";

        String token = jwtUtil.generateToken(email);
        Boolean isValid = jwtUtil.validateToken(token);

        assertFalse(isValid);
    }

    @Test
    void validateToken_InvalidToken_ReturnsFalse() {
        String invalidToken = "invalid.token.here";

        Boolean isValid = jwtUtil.validateToken(invalidToken);

        assertFalse(isValid);
    }

    @Test
    void validateToken_MalformedToken_ReturnsFalse() {
        String malformedToken = "not-a-jwt-token";

        Boolean isValid = jwtUtil.validateToken(malformedToken);

        assertFalse(isValid);
    }

    @Test
    void extractUserId_TokenWithoutUserId_ReturnsNull() {
        String email = "test@example.com";
        // Generate token without userId
        String token = jwtUtil.generateToken(email);

        UUID userId = jwtUtil.extractUserId(token);

        assertNull(userId);
    }
}

