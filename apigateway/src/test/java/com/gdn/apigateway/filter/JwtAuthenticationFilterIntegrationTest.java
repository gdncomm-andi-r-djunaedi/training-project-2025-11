package com.gdn.apigateway.filter;

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
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestConfig.class)
class JwtAuthenticationFilterIntegrationTest {

  @Autowired
  private WebTestClient webTestClient;

  @Autowired
  private TestConfig testConfig;

  @BeforeEach
  void setUp() {
    testConfig.clearBlacklist();
  }

  @Nested
  @DisplayName("Public Endpoints")
  class PublicEndpointsTests {

    @Test
    @DisplayName("Should allow access to /members/login without token")
    void shouldAllowLoginWithoutToken() {
      // Note: This will return 503 since downstream service isn't running
      // But that means it passed the auth filter successfully
      webTestClient.post()
          .uri("/members/login")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue("{\"username\":\"test\",\"password\":\"test\"}")
          .exchange()
          .expectStatus().is5xxServerError(); // 503 because downstream is unavailable
    }

    @Test
    @DisplayName("Should allow access to /members/register without token")
    void shouldAllowRegisterWithoutToken() {
      webTestClient.post()
          .uri("/members/register")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue("{\"username\":\"test\",\"password\":\"Test@123\",\"name\":\"Test\",\"address\":\"Test\"}")
          .exchange()
          .expectStatus().is5xxServerError(); // 503 because downstream is unavailable
    }

    @Test
    @DisplayName("Should allow access to /products without token")
    void shouldAllowProductsWithoutToken() {
      webTestClient.get()
          .uri("/products")
          .exchange()
          .expectStatus().is5xxServerError(); // 503 because downstream is unavailable
    }

    @Test
    @DisplayName("Should allow access to /products/{id} without token")
    void shouldAllowProductByIdWithoutToken() {
      webTestClient.get()
          .uri("/products/some-product-id")
          .exchange()
          .expectStatus().is5xxServerError(); // 503 because downstream is unavailable
    }
  }

  @Nested
  @DisplayName("Protected Endpoints - No Token")
  class ProtectedEndpointsNoTokenTests {

    @Test
    @DisplayName("Should reject /carts without token")
    void shouldRejectCartsWithoutToken() {
      webTestClient.get()
          .uri("/carts")
          .exchange()
          .expectStatus().isUnauthorized()
          .expectBody()
          .jsonPath("$.error").isEqualTo("Unauthorized")
          .jsonPath("$.message").isEqualTo("Missing or invalid Authorization header or cookie");
    }

    @Test
    @DisplayName("Should reject POST /carts without token")
    void shouldRejectAddToCartWithoutToken() {
      webTestClient.post()
          .uri("/carts")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue("{\"productId\":\"123\",\"quantity\":1}")
          .exchange()
          .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Should reject DELETE /carts/items/{id} without token")
    void shouldRejectDeleteCartItemWithoutToken() {
      webTestClient.delete()
          .uri("/carts/items/product-123")
          .exchange()
          .expectStatus().isUnauthorized();
    }
  }

  @Nested
  @DisplayName("Protected Endpoints - Valid Token")
  class ProtectedEndpointsValidTokenTests {

    @Test
    @DisplayName("Should allow /carts with valid token in Authorization header")
    void shouldAllowCartsWithValidTokenInHeader() {
      String token = TestJwtHelper.generateToken("member-123", "testuser");

      webTestClient.get()
          .uri("/carts")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
          .exchange()
          .expectStatus().is5xxServerError(); // 503 because downstream is unavailable
    }

    @Test
    @DisplayName("Should allow /carts with valid token in cookie")
    void shouldAllowCartsWithValidTokenInCookie() {
      String token = TestJwtHelper.generateToken("member-123", "testuser");

      webTestClient.get()
          .uri("/carts")
          .cookie("token", token)
          .exchange()
          .expectStatus().is5xxServerError(); // 503 because downstream is unavailable
    }

    @Test
    @DisplayName("Should allow POST /carts with valid token")
    void shouldAllowAddToCartWithValidToken() {
      String token = TestJwtHelper.generateToken("member-123", "testuser");

      webTestClient.post()
          .uri("/carts")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue("{\"productId\":\"123\",\"quantity\":1}")
          .exchange()
          .expectStatus().is5xxServerError(); // 503 because downstream is unavailable
    }
  }

  @Nested
  @DisplayName("Protected Endpoints - Invalid Token")
  class ProtectedEndpointsInvalidTokenTests {

    @Test
    @DisplayName("Should reject request with expired token")
    void shouldRejectExpiredToken() {
      String expiredToken = TestJwtHelper.generateExpiredToken("member-123", "testuser");

      webTestClient.get()
          .uri("/carts")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
          .exchange()
          .expectStatus().isUnauthorized()
          .expectBody()
          .jsonPath("$.message").isEqualTo("Invalid or expired token");
    }

    @Test
    @DisplayName("Should reject request with invalid signature token")
    void shouldRejectInvalidSignatureToken() {
      String invalidToken = TestJwtHelper.generateInvalidSignatureToken("member-123", "testuser");

      webTestClient.get()
          .uri("/carts")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken)
          .exchange()
          .expectStatus().isUnauthorized()
          .expectBody()
          .jsonPath("$.message").isEqualTo("Invalid or expired token");
    }

    @Test
    @DisplayName("Should reject request with malformed token")
    void shouldRejectMalformedToken() {
      webTestClient.get()
          .uri("/carts")
          .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-valid-jwt")
          .exchange()
          .expectStatus().isUnauthorized()
          .expectBody()
          .jsonPath("$.message").isEqualTo("Invalid or expired token");
    }

    @Test
    @DisplayName("Should reject request with Bearer but no token")
    void shouldRejectBearerWithNoToken() {
      webTestClient.get()
          .uri("/carts")
          .header(HttpHeaders.AUTHORIZATION, "Bearer ")
          .exchange()
          .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Should reject request without Bearer prefix")
    void shouldRejectWithoutBearerPrefix() {
      String token = TestJwtHelper.generateToken("member-123", "testuser");

      webTestClient.get()
          .uri("/carts")
          .header(HttpHeaders.AUTHORIZATION, token) // Missing "Bearer " prefix
          .exchange()
          .expectStatus().isUnauthorized()
          .expectBody()
          .jsonPath("$.message").isEqualTo("Missing or invalid Authorization header or cookie");
    }
  }

  @Nested
  @DisplayName("Blacklisted Token")
  class BlacklistedTokenTests {

    @Test
    @DisplayName("Should reject request with blacklisted token")
    void shouldRejectBlacklistedToken() {
      String token = TestJwtHelper.generateToken("member-123", "testuser");
      
      // Blacklist the token
      testConfig.blacklistToken(token);

      webTestClient.get()
          .uri("/carts")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
          .exchange()
          .expectStatus().isUnauthorized()
          .expectBody()
          .jsonPath("$.message").isEqualTo("Token has been revoked");
    }

    @Test
    @DisplayName("Should allow request after token is not blacklisted")
    void shouldAllowNonBlacklistedToken() {
      String token = TestJwtHelper.generateToken("member-123", "testuser");
      // Don't blacklist the token

      webTestClient.get()
          .uri("/carts")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
          .exchange()
          .expectStatus().is5xxServerError(); // 503 because downstream is unavailable (passed auth)
    }
  }
}

