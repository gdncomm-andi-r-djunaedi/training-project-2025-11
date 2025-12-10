package com.gdn.project.waroenk.gateway.security;

import com.gdn.project.waroenk.gateway.config.GatewayProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtUtil Unit Tests")
class JwtUtilTest {

  @Mock
  private GatewayProperties gatewayProperties;

  @Mock
  private GatewayProperties.JwtConfig jwtConfig;

  private JwtUtil jwtUtil;
  private SecretKey secretKey;
  private static final String TEST_SECRET = "test-secret-key-for-jwt-token-generation-minimum-256-bits";

  @BeforeEach
  void setUp() {
    when(gatewayProperties.getJwt()).thenReturn(jwtConfig);
    when(jwtConfig.getSecret()).thenReturn(TEST_SECRET);

    jwtUtil = new JwtUtil(gatewayProperties);
    jwtUtil.init();

    secretKey = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
  }

  private String createTestToken(Map<String, Object> claims, Date expiration) {
    return Jwts.builder()
        .claims(claims)
        .subject(claims.getOrDefault("sub", "test-user-id").toString())
        .issuedAt(new Date())
        .expiration(expiration)
        .signWith(secretKey)
        .compact();
  }

  @Nested
  @DisplayName("validateToken Tests")
  class ValidateTokenTests {

    @Test
    @DisplayName("Should validate valid token")
    void shouldValidateValidToken() {
      // Given
      String token = createTestToken(
          Map.of("userId", "user-123", "email", "test@example.com"),
          new Date(System.currentTimeMillis() + 3600000) // 1 hour
      );

      // When
      boolean isValid = jwtUtil.validateToken(token);

      // Then
      assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should reject expired token")
    void shouldRejectExpiredToken() {
      // Given
      String token = createTestToken(
          Map.of("userId", "user-123"),
          new Date(System.currentTimeMillis() - 1000) // Expired
      );

      // When
      boolean isValid = jwtUtil.validateToken(token);

      // Then
      assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject invalid token")
    void shouldRejectInvalidToken() {
      // When
      boolean isValid = jwtUtil.validateToken("invalid.token.here");

      // Then
      assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject token with wrong signature")
    void shouldRejectTokenWithWrongSignature() {
      // Given - create token with different key
      SecretKey wrongKey = Keys.hmacShaKeyFor(
          "wrong-secret-key-for-jwt-token-minimum-256-bits-padding".getBytes(StandardCharsets.UTF_8));
      String token = Jwts.builder()
          .subject("test-user")
          .expiration(new Date(System.currentTimeMillis() + 3600000))
          .signWith(wrongKey)
          .compact();

      // When
      boolean isValid = jwtUtil.validateToken(token);

      // Then
      assertThat(isValid).isFalse();
    }
  }

  @Nested
  @DisplayName("extractUserId Tests")
  class ExtractUserIdTests {

    @Test
    @DisplayName("Should extract userId from camelCase claim")
    void shouldExtractUserIdFromCamelCaseClaim() {
      // Given
      String token = createTestToken(
          Map.of("userId", "user-abc-123"),
          new Date(System.currentTimeMillis() + 3600000)
      );

      // When
      String userId = jwtUtil.extractUserId(token);

      // Then
      assertThat(userId).isEqualTo("user-abc-123");
    }

    @Test
    @DisplayName("Should extract user_id from snake_case claim")
    void shouldExtractUserIdFromSnakeCaseClaim() {
      // Given
      String token = createTestToken(
          Map.of("user_id", "user-xyz-456"),
          new Date(System.currentTimeMillis() + 3600000)
      );

      // When
      String userId = jwtUtil.extractUserId(token);

      // Then
      assertThat(userId).isEqualTo("user-xyz-456");
    }

    @Test
    @DisplayName("Should fallback to subject when userId not in claims")
    void shouldFallbackToSubject() {
      // Given
      String token = createTestToken(
          Map.of("email", "test@example.com", "sub", "subject-user-id"),
          new Date(System.currentTimeMillis() + 3600000)
      );

      // When
      String userId = jwtUtil.extractUserId(token);

      // Then
      assertThat(userId).isEqualTo("subject-user-id");
    }
  }

  @Nested
  @DisplayName("extractUsername Tests")
  class ExtractUsernameTests {

    @Test
    @DisplayName("Should extract email as username")
    void shouldExtractEmailAsUsername() {
      // Given
      String token = createTestToken(
          Map.of("email", "john@example.com", "phone", "+6281234567890"),
          new Date(System.currentTimeMillis() + 3600000)
      );

      // When
      String username = jwtUtil.extractUsername(token);

      // Then
      assertThat(username).isEqualTo("john@example.com");
    }

    @Test
    @DisplayName("Should fallback to phone when email not present")
    void shouldFallbackToPhone() {
      // Given
      String token = createTestToken(
          Map.of("phone", "+6281234567890"),
          new Date(System.currentTimeMillis() + 3600000)
      );

      // When
      String username = jwtUtil.extractUsername(token);

      // Then
      assertThat(username).isEqualTo("+6281234567890");
    }

    @Test
    @DisplayName("Should fallback to subject when no email or phone")
    void shouldFallbackToSubjectWhenNoEmailOrPhone() {
      // Given
      String token = createTestToken(
          Map.of("sub", "user-id-fallback"),
          new Date(System.currentTimeMillis() + 3600000)
      );

      // When
      String username = jwtUtil.extractUsername(token);

      // Then
      assertThat(username).isEqualTo("user-id-fallback");
    }
  }

  @Nested
  @DisplayName("extractRoles Tests")
  class ExtractRolesTests {

    @Test
    @DisplayName("Should extract roles from token")
    void shouldExtractRoles() {
      // Given
      String token = createTestToken(
          Map.of("roles", List.of("ADMIN", "USER")),
          new Date(System.currentTimeMillis() + 3600000)
      );

      // When
      List<String> roles = jwtUtil.extractRoles(token);

      // Then
      assertThat(roles).containsExactly("ADMIN", "USER");
    }

    @Test
    @DisplayName("Should return null when no roles claim")
    void shouldReturnNullWhenNoRoles() {
      // Given
      String token = createTestToken(
          Map.of("userId", "user-123"),
          new Date(System.currentTimeMillis() + 3600000)
      );

      // When
      List<String> roles = jwtUtil.extractRoles(token);

      // Then
      assertThat(roles).isNull();
    }
  }

  @Nested
  @DisplayName("extractExpiration Tests")
  class ExtractExpirationTests {

    @Test
    @DisplayName("Should extract expiration date")
    void shouldExtractExpiration() {
      // Given
      Date expiration = new Date(System.currentTimeMillis() + 3600000);
      String token = createTestToken(Map.of("userId", "user-123"), expiration);

      // When
      Date extractedExpiration = jwtUtil.extractExpiration(token);

      // Then
      // Allow 1 second tolerance for timing
      assertThat(extractedExpiration.getTime()).isCloseTo(expiration.getTime(), org.assertj.core.api.Assertions.within(1000L));
    }
  }

  @Nested
  @DisplayName("isTokenExpired Tests")
  class IsTokenExpiredTests {

    @Test
    @DisplayName("Should return false for valid non-expired token")
    void shouldReturnFalseForValidToken() {
      // Given
      String token = createTestToken(
          Map.of("userId", "user-123"),
          new Date(System.currentTimeMillis() + 3600000)
      );

      // When
      boolean isExpired = jwtUtil.isTokenExpired(token);

      // Then
      assertThat(isExpired).isFalse();
    }

    @Test
    @DisplayName("Should return true for expired token")
    void shouldReturnTrueForExpiredToken() {
      // Given
      String token = createTestToken(
          Map.of("userId", "user-123"),
          new Date(System.currentTimeMillis() - 1000)
      );

      // When
      boolean isExpired = jwtUtil.isTokenExpired(token);

      // Then
      assertThat(isExpired).isTrue();
    }
  }

  @Nested
  @DisplayName("validateTokenWithClaims Tests")
  class ValidateTokenWithClaimsTests {

    @Test
    @DisplayName("Should validate token with matching userId")
    void shouldValidateTokenWithMatchingUserId() {
      // Given
      String userId = "user-abc-123";
      String token = createTestToken(
          Map.of("userId", userId),
          new Date(System.currentTimeMillis() + 3600000)
      );

      // When
      boolean isValid = jwtUtil.validateTokenWithClaims(token, userId);

      // Then
      assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should reject token with non-matching userId")
    void shouldRejectTokenWithNonMatchingUserId() {
      // Given
      String token = createTestToken(
          Map.of("userId", "user-123"),
          new Date(System.currentTimeMillis() + 3600000)
      );

      // When
      boolean isValid = jwtUtil.validateTokenWithClaims(token, "different-user");

      // Then
      assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject expired token even with matching userId")
    void shouldRejectExpiredTokenEvenWithMatchingUserId() {
      // Given
      String userId = "user-123";
      String token = createTestToken(
          Map.of("userId", userId),
          new Date(System.currentTimeMillis() - 1000)
      );

      // When
      boolean isValid = jwtUtil.validateTokenWithClaims(token, userId);

      // Then
      assertThat(isValid).isFalse();
    }
  }

  @Nested
  @DisplayName("init Tests")
  class InitTests {

    @Test
    @DisplayName("Should initialize with Base64 encoded secret")
    void shouldInitializeWithBase64EncodedSecret() {
      // Given
      String base64Secret = Base64.getEncoder().encodeToString(
          "test-base64-secret-key-minimum-256-bits-padding-required".getBytes(StandardCharsets.UTF_8)
      );
      when(jwtConfig.getSecret()).thenReturn(base64Secret);

      // When
      JwtUtil jwtUtilWithBase64 = new JwtUtil(gatewayProperties);
      jwtUtilWithBase64.init();

      // Then - no exception means success
      // Create a token to verify it works
      String token = Jwts.builder()
          .subject("test")
          .expiration(new Date(System.currentTimeMillis() + 3600000))
          .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(base64Secret)))
          .compact();

      assertThat(jwtUtilWithBase64.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("Should use default key when secret is blank")
    void shouldUseDefaultKeyWhenSecretIsBlank() {
      // Given
      when(jwtConfig.getSecret()).thenReturn("");

      // When
      JwtUtil jwtUtilWithDefault = new JwtUtil(gatewayProperties);
      jwtUtilWithDefault.init();

      // Then - should not throw, uses default key
      // We can't easily test the actual default key, but we can verify it initializes
    }
  }
}


