package com.gdn.apigateway.controller;

import com.gdn.apigateway.config.TestConfig;
import com.gdn.apigateway.util.TestJwtHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestConfig.class)
class AuthControllerIntegrationTest {

  @Autowired
  private WebTestClient webTestClient;

  @Autowired
  private TestConfig testConfig;

  @BeforeEach
  void setUp() {
    testConfig.clearBlacklist();
  }

  @Nested
  @DisplayName("POST /auth/logout")
  class LogoutTests {

    @Test
    @DisplayName("Should logout successfully with valid token in header")
    void shouldLogoutWithValidTokenInHeader() {
      String token = TestJwtHelper.generateToken("member-123", "testuser");

      webTestClient.post()
          .uri("/auth/logout")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
          .exchange()
          .expectStatus().isOk()
          .expectBody()
          .jsonPath("$.message").isEqualTo("Logged out successfully")
          .jsonPath("$.username").isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should logout successfully with valid token in cookie")
    void shouldLogoutWithValidTokenInCookie() {
      String token = TestJwtHelper.generateToken("member-123", "testuser");

      webTestClient.post()
          .uri("/auth/logout")
          .cookie("token", token)
          .exchange()
          .expectStatus().isOk()
          .expectBody()
          .jsonPath("$.message").isEqualTo("Logged out successfully")
          .jsonPath("$.username").isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should clear cookie on logout")
    void shouldClearCookieOnLogout() {
      String token = TestJwtHelper.generateToken("member-123", "testuser");

      webTestClient.post()
          .uri("/auth/logout")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
          .exchange()
          .expectStatus().isOk()
          .expectHeader().valueMatches(HttpHeaders.SET_COOKIE, ".*token=;.*Max-Age=0.*");
    }

    @Test
    @DisplayName("Should blacklist token after logout")
    void shouldBlacklistTokenAfterLogout() {
      String token = TestJwtHelper.generateToken("member-123", "testuser");

      // First logout
      webTestClient.post()
          .uri("/auth/logout")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
          .exchange()
          .expectStatus().isOk();

      // Then try to access protected endpoint with same token
      webTestClient.get()
          .uri("/carts")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
          .exchange()
          .expectStatus().isUnauthorized()
          .expectBody()
          .jsonPath("$.message").isEqualTo("Token has been revoked");
    }

    @Test
    @DisplayName("Should return error when no token provided")
    void shouldReturnErrorWhenNoToken() {
      webTestClient.post()
          .uri("/auth/logout")
          .exchange()
          .expectStatus().isBadRequest()
          .expectBody()
          .jsonPath("$.error").isEqualTo("No token provided");
    }

    @Test
    @DisplayName("Should return error for expired token")
    void shouldReturnErrorForExpiredToken() {
      String expiredToken = TestJwtHelper.generateExpiredToken("member-123", "testuser");

      webTestClient.post()
          .uri("/auth/logout")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
          .exchange()
          .expectStatus().isBadRequest()
          .expectBody()
          .jsonPath("$.error").isEqualTo("Invalid token");
    }

    @Test
    @DisplayName("Should return error for invalid token")
    void shouldReturnErrorForInvalidToken() {
      webTestClient.post()
          .uri("/auth/logout")
          .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
          .exchange()
          .expectStatus().isBadRequest()
          .expectBody()
          .jsonPath("$.error").isEqualTo("Invalid token");
    }

    @Test
    @DisplayName("Should return error for token with invalid signature")
    void shouldReturnErrorForInvalidSignatureToken() {
      String invalidToken = TestJwtHelper.generateInvalidSignatureToken("member-123", "testuser");

      webTestClient.post()
          .uri("/auth/logout")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken)
          .exchange()
          .expectStatus().isBadRequest()
          .expectBody()
          .jsonPath("$.error").isEqualTo("Invalid token");
    }
  }

  @Nested
  @DisplayName("Token Priority")
  class TokenPriorityTests {

    @Test
    @DisplayName("Should prefer Authorization header over cookie")
    void shouldPreferHeaderOverCookie() {
      String headerToken = TestJwtHelper.generateToken("header-member", "headeruser");
      String cookieToken = TestJwtHelper.generateToken("cookie-member", "cookieuser");

      webTestClient.post()
          .uri("/auth/logout")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + headerToken)
          .cookie("token", cookieToken)
          .exchange()
          .expectStatus().isOk()
          .expectBody()
          .jsonPath("$.username").isEqualTo("headeruser");
    }

    @Test
    @DisplayName("Should use cookie when header is missing")
    void shouldUseCookieWhenHeaderMissing() {
      String token = TestJwtHelper.generateToken("member-123", "cookieuser");

      webTestClient.post()
          .uri("/auth/logout")
          .cookie("token", token)
          .exchange()
          .expectStatus().isOk()
          .expectBody()
          .jsonPath("$.username").isEqualTo("cookieuser");
    }
  }
}

