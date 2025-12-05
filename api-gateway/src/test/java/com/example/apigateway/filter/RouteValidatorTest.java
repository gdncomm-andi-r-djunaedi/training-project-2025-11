package com.example.apigateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RouteValidatorTest {

    private final RouteValidator validator = new RouteValidator();

    @Test
    void isSecured_shouldReturnFalse_forAuthRegister() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/auth/register").build();
        assertFalse(validator.isSecured.test(request));
    }

    @Test
    void isSecured_shouldReturnFalse_forAuthLogin() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/auth/login").build();
        assertFalse(validator.isSecured.test(request));
    }

    @Test
    void isSecured_shouldReturnFalse_forAuthLogout() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/auth/logout").build();
        assertFalse(validator.isSecured.test(request));
    }

    @Test
    void isSecured_shouldReturnFalse_forProductRoot() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/product").build();
        assertFalse(validator.isSecured.test(request));
    }

    @Test
    void isSecured_shouldReturnFalse_forProductSubPaths() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/product/123").build();
        assertFalse(validator.isSecured.test(request));

        request = MockServerHttpRequest.get("/product/123/details").build();
        assertFalse(validator.isSecured.test(request));
    }

    @Test
    void isSecured_shouldReturnTrue_forApiProducts() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/products").build();
        assertTrue(validator.isSecured.test(request));
    }

    @Test
    void isSecured_shouldReturnTrue_forApiCart() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/cart").build();
        assertTrue(validator.isSecured.test(request));

        request = MockServerHttpRequest.get("/api/cart/123").build();
        assertTrue(validator.isSecured.test(request));
    }

    @Test
    void isSecured_shouldReturnTrue_forOtherPaths() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/users").build();
        assertTrue(validator.isSecured.test(request));

        request = MockServerHttpRequest.get("/admin/dashboard").build();
        assertTrue(validator.isSecured.test(request));
    }

    @Test
    void isSecured_shouldReturnTrue_forRootPath() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/").build();
        assertTrue(validator.isSecured.test(request));
    }
}
