package com.example.apigateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RouteValidatorTest {

    private final RouteValidator routeValidator = new RouteValidator();

    @Test
    void isSecured_shouldReturnFalse_forWhitelistedEndpoints() {
        // Test Swagger UI paths
        assertFalse(routeValidator.isSecured.test(MockServerHttpRequest.get("/swagger-ui.html").build()));
        assertFalse(routeValidator.isSecured.test(MockServerHttpRequest.get("/swagger-ui/index.html").build()));
        assertFalse(routeValidator.isSecured.test(MockServerHttpRequest.get("/v3/api-docs").build()));
        assertFalse(routeValidator.isSecured.test(MockServerHttpRequest.get("/v3/api-docs/swagger-config").build()));
        assertFalse(routeValidator.isSecured.test(MockServerHttpRequest.get("/webjars/swagger-ui/index.html").build()));

        // Test Auth paths
        assertFalse(routeValidator.isSecured.test(MockServerHttpRequest.get("/auth/login").build()));
        assertFalse(routeValidator.isSecured.test(MockServerHttpRequest.get("/auth/register").build()));

        // Test Product paths (Assuming configured as open)
        assertFalse(routeValidator.isSecured.test(MockServerHttpRequest.get("/api/products").build()));
    }

    @Test
    void isSecured_shouldReturnTrue_forProtectedEndpoints() {
        assertTrue(routeValidator.isSecured.test(MockServerHttpRequest.get("/api/cart").build()));
        assertTrue(routeValidator.isSecured.test(MockServerHttpRequest.get("/api/cart/1").build()));
    }
}
