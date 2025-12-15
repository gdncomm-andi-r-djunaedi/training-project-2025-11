package com.gdn.faurihakim.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtUtil Security Tests")
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String TEST_SECRET = "mySecretKey12345678901234567890123456789012";
    private static final long TEST_EXPIRATION = 3600000L; // 1 hour
    private static final String TEST_USER_ID = "test-user-123";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", TEST_EXPIRATION);
    }

    @Test
    @DisplayName("Should generate valid JWT token with correct claims")
    void testGenerateToken_Success() {
        // Act
        String token = jwtUtil.generateToken(TEST_USER_ID);

        // Assert
        assertThat(token).isNotNull().isNotEmpty();

        // Verify token structure (header.payload.signature)
        String[] parts = token.split("\\.");
        assertThat(parts).hasSize(3);

        // Verify claims
        Key key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes());
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertThat(claims.getSubject()).isEqualTo(TEST_USER_ID);
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isNotNull();
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    @DisplayName("Should validate correct JWT token")
    void testValidateToken_ValidToken_ReturnsTrue() {
        // Arrange
        String token = jwtUtil.generateToken(TEST_USER_ID);

        // Act
        boolean isValid = jwtUtil.validateToken(token);

        // Assert
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should reject expired JWT token")
    void testValidateToken_ExpiredToken_ReturnsFalse() {
        // Arrange - Create expired token
        Key key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes());
        String expiredToken = Jwts.builder()
                .setSubject(TEST_USER_ID)
                .setIssuedAt(new Date(System.currentTimeMillis() - 7200000)) // 2 hours ago
                .setExpiration(new Date(System.currentTimeMillis() - 3600000)) // 1 hour ago (expired)
                .signWith(key)
                .compact();

        // Act
        boolean isValid = jwtUtil.validateToken(expiredToken);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject JWT token with invalid signature")
    void testValidateToken_InvalidSignature_ReturnsFalse() {
        // Arrange - Create token with different secret
        String differentSecret = "differentSecret12345678901234567890123";
        Key wrongKey = Keys.hmacShaKeyFor(differentSecret.getBytes());
        String invalidToken = Jwts.builder()
                .setSubject(TEST_USER_ID)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + TEST_EXPIRATION))
                .signWith(wrongKey)
                .compact();

        // Act
        boolean isValid = jwtUtil.validateToken(invalidToken);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject malformed JWT token")
    void testValidateToken_MalformedToken_ReturnsFalse() {
        // Arrange
        String malformedToken = "not.a.valid.jwt.token";

        // Act
        boolean isValid = jwtUtil.validateToken(malformedToken);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject null JWT token")
    void testValidateToken_NullToken_ReturnsFalse() {
        // Act
        boolean isValid = jwtUtil.validateToken(null);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject empty JWT token")
    void testValidateToken_EmptyToken_ReturnsFalse() {
        // Act
        boolean isValid = jwtUtil.validateToken("");

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should extract correct user ID from valid token")
    void testExtractUserId_ValidToken_ReturnsUserId() {
        // Arrange
        String token = jwtUtil.generateToken(TEST_USER_ID);

        // Act
        String extractedUserId = jwtUtil.extractUserId(token);

        // Assert
        assertThat(extractedUserId).isEqualTo(TEST_USER_ID);
    }

    @Test
    @DisplayName("Should handle token with special characters in user ID")
    void testGenerateAndExtract_SpecialCharactersInUserId() {
        // Arrange
        String specialUserId = "user-123_test@example.com";

        // Act
        String token = jwtUtil.generateToken(specialUserId);
        String extractedUserId = jwtUtil.extractUserId(token);

        // Assert
        assertThat(extractedUserId).isEqualTo(specialUserId);
    }

    @Test
    @DisplayName("Should generate different tokens for same user at different times")
    void testGenerateToken_DifferentTimestamps_DifferentTokens() throws InterruptedException {
        // Arrange & Act
        String token1 = jwtUtil.generateToken(TEST_USER_ID);
        Thread.sleep(1000); // Wait 1 second to ensure different timestamp (JWT uses seconds)
        String token2 = jwtUtil.generateToken(TEST_USER_ID);

        // Assert
        assertThat(token1).isNotEqualTo(token2);

        // Both should be valid
        assertThat(jwtUtil.validateToken(token1)).isTrue();
        assertThat(jwtUtil.validateToken(token2)).isTrue();

        // Both should extract same user ID
        assertThat(jwtUtil.extractUserId(token1)).isEqualTo(TEST_USER_ID);
        assertThat(jwtUtil.extractUserId(token2)).isEqualTo(TEST_USER_ID);
    }

    @Test
    @DisplayName("Should generate different tokens for different users")
    void testGenerateToken_DifferentUsers_DifferentTokens() {
        // Arrange
        String userId1 = "user-1";
        String userId2 = "user-2";

        // Act
        String token1 = jwtUtil.generateToken(userId1);
        String token2 = jwtUtil.generateToken(userId2);

        // Assert
        assertThat(token1).isNotEqualTo(token2);
        assertThat(jwtUtil.extractUserId(token1)).isEqualTo(userId1);
        assertThat(jwtUtil.extractUserId(token2)).isEqualTo(userId2);
    }

    @Test
    @DisplayName("Should reject token tampering attempt")
    void testValidateToken_TamperedPayload_ReturnsFalse() {
        // Arrange
        String validToken = jwtUtil.generateToken(TEST_USER_ID);

        // Tamper with token by modifying payload (change a character in the middle)
        String[] parts = validToken.split("\\.");
        String tamperedPayload = parts[1].substring(0, parts[1].length() - 1) + "X";
        String tamperedToken = parts[0] + "." + tamperedPayload + "." + parts[2];

        // Act
        boolean isValid = jwtUtil.validateToken(tamperedToken);

        // Assert
        assertThat(isValid).isFalse();
    }
}
