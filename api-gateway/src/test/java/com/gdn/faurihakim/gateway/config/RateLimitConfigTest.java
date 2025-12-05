package com.gdn.faurihakim.gateway.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Rate Limit Configuration Tests")
class RateLimitConfigTest {

    @Autowired
    private KeyResolver userKeyResolver;

    @Test
    @DisplayName("Should use user ID when X-User-Id header is present")
    void testKeyResolver_WithUserId() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/products")
                .header("X-User-Id", "user-123")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act & Assert
        StepVerifier.create(userKeyResolver.resolve(exchange))
                .expectNext("user-123")
                .verifyComplete();
    }

    @Test
    @DisplayName("Should use IP address when X-User-Id header is missing")
    void testKeyResolver_WithoutUserId() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/products")
                .remoteAddress(new java.net.InetSocketAddress("192.168.1.1", 8080))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act & Assert
        StepVerifier.create(userKeyResolver.resolve(exchange))
                .assertNext(key -> assertThat(key).contains("192.168.1.1"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle missing remote address gracefully")
    void testKeyResolver_NoRemoteAddress() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/products")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act & Assert
        StepVerifier.create(userKeyResolver.resolve(exchange))
                .expectNext("unknown")
                .verifyComplete();
    }
}
