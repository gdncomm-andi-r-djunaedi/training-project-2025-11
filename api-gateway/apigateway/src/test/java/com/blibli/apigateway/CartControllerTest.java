package com.blibli.apigateway;

import com.blibli.apigateway.client.CartClient;
import com.blibli.apigateway.controller.CartController;
import com.blibli.apigateway.dto.request.CartDto;
import com.blibli.apigateway.dto.request.CartItemDto;
import com.blibli.apigateway.dto.response.CartResponseDto;
import com.blibli.apigateway.dto.response.ErrorResponse;
import com.blibli.apigateway.dto.response.ViewCartResponseDto;
import com.blibli.apigateway.service.JwtService;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CartControllerTest {

    @Mock
    private CartClient cartClient;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private CartController cartController;

    private static final String VALID_TOKEN = "valid-token-123";
    private static final String BEARER_TOKEN = "Bearer " + VALID_TOKEN;

    @BeforeEach
    void setUp() {
        cartController = new CartController(cartClient, jwtService);
    }


    private static class TestFeignException extends FeignException {
        private final int statusCode;
        
        public TestFeignException(int status, String message, byte[] content) {
            super(status, message, (Throwable) null, content, (java.util.Map<String, java.util.Collection<String>>) null);
            this.statusCode = status;
        }
        
        @Override
        public int status() {
            return statusCode;
        }
    }

    private void setupValidToken() {
        when(jwtService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(false);
        when(jwtService.isTokenExpired(VALID_TOKEN)).thenReturn(false);
        when(jwtService.validateToken(VALID_TOKEN)).thenReturn(true);
    }

    @Test
    void testAddToCart_Success() {
        CartDto request = new CartDto("PRD-00001", 2);
        CartResponseDto response = new CartResponseDto(
            "Product added to cart successfully",
            "SUCCESS",
            "ADD",
            "PRD-00001",
            2
        );

        setupValidToken();
        when(cartClient.addToCart(any(CartDto.class), anyString())).thenReturn(response);

        ResponseEntity<?> result = cartController.addToCart(request, BEARER_TOKEN);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof CartResponseDto);
        
        CartResponseDto responseBody = (CartResponseDto) result.getBody();
        assertEquals("SUCCESS", responseBody.getStatus());
        assertEquals("PRD-00001", responseBody.getProductCode());
        assertEquals(2, responseBody.getQuantity());
    }

    @Test
    void testAddToCart_MissingAuthorizationHeader() {
        CartDto request = new CartDto("PRD-00001", 2);

        ResponseEntity<?> result = cartController.addToCart(request, null);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof String);
        assertTrue(result.getBody().toString().contains("Missing Authorization header"));
    }

    @Test
    void testAddToCart_ExpiredToken() {
        CartDto request = new CartDto("PRD-00001", 2);

        when(jwtService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(false);
        when(jwtService.isTokenExpired(VALID_TOKEN)).thenReturn(true);

        ResponseEntity<?> result = cartController.addToCart(request, BEARER_TOKEN);

        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof ErrorResponse);
        
        ErrorResponse errorResponse = (ErrorResponse) result.getBody();
        assertEquals("UNAUTHORIZED", errorResponse.getStatus());
        assertEquals(401, errorResponse.getCode());
        assertEquals("Token has expired. Please login again.", errorResponse.getMessage());
        assertEquals("/api/cart/add", errorResponse.getPath());
    }

    @Test
    void testAddToCart_BlacklistedToken() {
        CartDto request = new CartDto("PRD-00001", 2);

        when(jwtService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(true);

        ResponseEntity<?> result = cartController.addToCart(request, BEARER_TOKEN);

        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof ErrorResponse);
        
        ErrorResponse errorResponse = (ErrorResponse) result.getBody();
        assertEquals("UNAUTHORIZED", errorResponse.getStatus());
        assertEquals(401, errorResponse.getCode());
        assertEquals("Token has been invalidated. Please login again.", errorResponse.getMessage());
        assertEquals("/api/cart/add", errorResponse.getPath());
    }

    @Test
    void testAddToCart_InvalidToken() {
        CartDto request = new CartDto("PRD-00001", 2);

        when(jwtService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(false);
        when(jwtService.isTokenExpired(VALID_TOKEN)).thenReturn(false);
        when(jwtService.validateToken(VALID_TOKEN)).thenReturn(false);

        ResponseEntity<?> result = cartController.addToCart(request, BEARER_TOKEN);

        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof ErrorResponse);
        
        ErrorResponse errorResponse = (ErrorResponse) result.getBody();
        assertEquals("UNAUTHORIZED", errorResponse.getStatus());
        assertEquals(401, errorResponse.getCode());
        assertEquals("Invalid token. Please login again.", errorResponse.getMessage());
        assertEquals("/api/cart/add", errorResponse.getPath());
    }

    @Test
    void testAddToCart_MalformedToken() {
        CartDto request = new CartDto("PRD-00001", 2);

        ResponseEntity<?> result = cartController.addToCart(request, "InvalidToken");

        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof ErrorResponse);
        
        ErrorResponse errorResponse = (ErrorResponse) result.getBody();
        assertEquals("UNAUTHORIZED", errorResponse.getStatus());
        assertEquals(401, errorResponse.getCode());
    }

    @Test
    void testAddToCart_ProductNotFound() {
        CartDto request = new CartDto("PRD-NOTFOUND", 1);

        setupValidToken();
        FeignException exception = new TestFeignException(
            404,
            "Not Found",
            "Product not found".getBytes(StandardCharsets.UTF_8)
        );
        when(cartClient.addToCart(any(CartDto.class), anyString())).thenThrow(exception);

        ResponseEntity<?> result = cartController.addToCart(request, BEARER_TOKEN);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof String);
        assertTrue(result.getBody().toString().contains("Cart service error"));
    }

    @Test
    void testAddToCart_CartServiceUnavailable() {
        CartDto request = new CartDto("PRD-00001", 1);

        setupValidToken();
        FeignException exception = new TestFeignException(
            503,
            "Service Unavailable",
            "Service unavailable".getBytes(StandardCharsets.UTF_8)
        );
        when(cartClient.addToCart(any(CartDto.class), anyString())).thenThrow(exception);

        ResponseEntity<?> result = cartController.addToCart(request, BEARER_TOKEN);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof String);
        assertTrue(result.getBody().toString().contains("Cart service error"));
    }


    @Test
    void testViewCart_Success_WithItems() {
        List<CartItemDto> items = new ArrayList<>();
        items.add(new CartItemDto("PRD-00001", 2, "PRD-00001", "Product 1", 99.99, "http://image1.jpg"));
        items.add(new CartItemDto("PRD-00002", 1, "PRD-00002", "Product 2", 149.99, "http://image2.jpg"));
        
        ViewCartResponseDto response = new ViewCartResponseDto(
            "Cart retrieved successfully",
            "SUCCESS",
            "VIEW",
            3,
            items
        );

        setupValidToken();
        when(cartClient.viewCart(anyString())).thenReturn(response);

        ResponseEntity<?> result = cartController.viewCart(BEARER_TOKEN);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof ViewCartResponseDto);
        
        ViewCartResponseDto responseBody = (ViewCartResponseDto) result.getBody();
        assertEquals("SUCCESS", responseBody.getStatus());
        assertEquals(3, responseBody.getTotalCartQuantity());
        assertNotNull(responseBody.getItems());
        assertEquals(2, responseBody.getItems().size());
        assertEquals("PRD-00001", responseBody.getItems().get(0).getProductCode());
        assertEquals("http://image1.jpg", responseBody.getItems().get(0).getImageUrl());
    }

    @Test
    void testViewCart_Success_EmptyCart() {
        ViewCartResponseDto response = new ViewCartResponseDto(
            "Cart retrieved successfully",
            "SUCCESS",
            "VIEW",
            0,
            new ArrayList<>()
        );

        setupValidToken();
        when(cartClient.viewCart(anyString())).thenReturn(response);

        ResponseEntity<?> result = cartController.viewCart(BEARER_TOKEN);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof ViewCartResponseDto);
        
        ViewCartResponseDto responseBody = (ViewCartResponseDto) result.getBody();
        assertEquals("SUCCESS", responseBody.getStatus());
        assertEquals(0, responseBody.getTotalCartQuantity());
        assertNotNull(responseBody.getItems());
        assertTrue(responseBody.getItems().isEmpty());
    }

    @Test
    void testViewCart_MissingAuthorizationHeader() {
        ResponseEntity<?> result = cartController.viewCart(null);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof String);
        assertTrue(result.getBody().toString().contains("Missing Authorization header"));
    }

    @Test
    void testViewCart_ExpiredToken() {
        when(jwtService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(false);
        when(jwtService.isTokenExpired(VALID_TOKEN)).thenReturn(true);

        ResponseEntity<?> result = cartController.viewCart(BEARER_TOKEN);

        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof ErrorResponse);
        
        ErrorResponse errorResponse = (ErrorResponse) result.getBody();
        assertEquals("UNAUTHORIZED", errorResponse.getStatus());
        assertEquals(401, errorResponse.getCode());
        assertEquals("Token has expired. Please login again.", errorResponse.getMessage());
        assertEquals("/api/cart/view", errorResponse.getPath());
    }

    @Test
    void testViewCart_BlacklistedToken() {
        when(jwtService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(true);

        ResponseEntity<?> result = cartController.viewCart(BEARER_TOKEN);

        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof ErrorResponse);
        
        ErrorResponse errorResponse = (ErrorResponse) result.getBody();
        assertEquals("UNAUTHORIZED", errorResponse.getStatus());
        assertEquals(401, errorResponse.getCode());
        assertEquals("Token has been invalidated. Please login again.", errorResponse.getMessage());
        assertEquals("/api/cart/view", errorResponse.getPath());
    }

    @Test
    void testViewCart_InvalidToken() {
        when(jwtService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(false);
        when(jwtService.isTokenExpired(VALID_TOKEN)).thenReturn(false);
        when(jwtService.validateToken(VALID_TOKEN)).thenReturn(false);

        ResponseEntity<?> result = cartController.viewCart(BEARER_TOKEN);

        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof ErrorResponse);
        
        ErrorResponse errorResponse = (ErrorResponse) result.getBody();
        assertEquals("UNAUTHORIZED", errorResponse.getStatus());
        assertEquals(401, errorResponse.getCode());
        assertEquals("Invalid token. Please login again.", errorResponse.getMessage());
        assertEquals("/api/cart/view", errorResponse.getPath());
    }

    @Test
    void testViewCart_CartServiceUnavailable() {
        setupValidToken();
        FeignException exception = new TestFeignException(
            503,
            "Service Unavailable",
            "Service unavailable".getBytes(StandardCharsets.UTF_8)
        );
        when(cartClient.viewCart(anyString())).thenThrow(exception);

        ResponseEntity<?> result = cartController.viewCart(BEARER_TOKEN);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof String);
        assertTrue(result.getBody().toString().contains("Cart service error"));
    }

    @Test
    void testViewCart_ResponseStructure_NoUnwantedFields() {
        List<CartItemDto> items = new ArrayList<>();
        items.add(new CartItemDto("PRD-00001", 2, "PRD-00001", "Product 1", 99.99, "http://image1.jpg"));
        
        ViewCartResponseDto response = new ViewCartResponseDto(
            "Cart retrieved successfully",
            "SUCCESS",
            "VIEW",
            2,
            items
        );

        setupValidToken();
        when(cartClient.viewCart(anyString())).thenReturn(response);

        ResponseEntity<?> result = cartController.viewCart(BEARER_TOKEN);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof ViewCartResponseDto);
        
        ViewCartResponseDto responseBody = (ViewCartResponseDto) result.getBody();
        assertNotNull(responseBody.getMessage());
        assertNotNull(responseBody.getStatus());
        assertNotNull(responseBody.getAction());
        assertNotNull(responseBody.getTotalCartQuantity());
        assertNotNull(responseBody.getItems());
        CartItemDto item = responseBody.getItems().get(0);
        assertNotNull(item.getProductCode());
        assertNotNull(item.getQuantity());
        assertNotNull(item.getProductId());
        assertNotNull(item.getProductName());
        assertNotNull(item.getPrice());
        assertNotNull(item.getImageUrl());
    }


    @Test
    void testClearCart_Success() {
        String successMessage = "Cart cleared successfully";

        setupValidToken();
        when(cartClient.clearCart(anyString())).thenReturn(successMessage);

        ResponseEntity<?> result = cartController.clearCart(BEARER_TOKEN);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(successMessage, result.getBody());
    }

    @Test
    void testClearCart_Success_EmptyCart() {
        String successMessage = "Cart cleared successfully";

        setupValidToken();
        when(cartClient.clearCart(anyString())).thenReturn(successMessage);

        ResponseEntity<?> result = cartController.clearCart(BEARER_TOKEN);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(successMessage, result.getBody());
    }

    @Test
    void testClearCart_MissingAuthorizationHeader() {
        ResponseEntity<?> result = cartController.clearCart(null);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof String);
        assertTrue(result.getBody().toString().contains("Missing Authorization header"));
    }

    @Test
    void testClearCart_ExpiredToken() {
        when(jwtService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(false);
        when(jwtService.isTokenExpired(VALID_TOKEN)).thenReturn(true);

        ResponseEntity<?> result = cartController.clearCart(BEARER_TOKEN);

        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof ErrorResponse);
        
        ErrorResponse errorResponse = (ErrorResponse) result.getBody();
        assertEquals("UNAUTHORIZED", errorResponse.getStatus());
        assertEquals(401, errorResponse.getCode());
        assertEquals("Token has expired. Please login again.", errorResponse.getMessage());
        assertEquals("/api/cart/clear", errorResponse.getPath());
    }

    @Test
    void testClearCart_BlacklistedToken() {
        when(jwtService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(true);

        ResponseEntity<?> result = cartController.clearCart(BEARER_TOKEN);

        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof ErrorResponse);
        
        ErrorResponse errorResponse = (ErrorResponse) result.getBody();
        assertEquals("UNAUTHORIZED", errorResponse.getStatus());
        assertEquals(401, errorResponse.getCode());
        assertEquals("Token has been invalidated. Please login again.", errorResponse.getMessage());
        assertEquals("/api/cart/clear", errorResponse.getPath());
    }

    @Test
    void testClearCart_InvalidToken() {
        when(jwtService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(false);
        when(jwtService.isTokenExpired(VALID_TOKEN)).thenReturn(false);
        when(jwtService.validateToken(VALID_TOKEN)).thenReturn(false);

        ResponseEntity<?> result = cartController.clearCart(BEARER_TOKEN);

        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof ErrorResponse);
        
        ErrorResponse errorResponse = (ErrorResponse) result.getBody();
        assertEquals("UNAUTHORIZED", errorResponse.getStatus());
        assertEquals(401, errorResponse.getCode());
        assertEquals("Invalid token. Please login again.", errorResponse.getMessage());
        assertEquals("/api/cart/clear", errorResponse.getPath());
    }

    @Test
    void testClearCart_CartServiceUnavailable() {
        setupValidToken();
        FeignException exception = new TestFeignException(
            503,
            "Service Unavailable",
            "Service unavailable".getBytes(StandardCharsets.UTF_8)
        );
        when(cartClient.clearCart(anyString())).thenThrow(exception);

        ResponseEntity<?> result = cartController.clearCart(BEARER_TOKEN);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, result.getStatusCode());
    }


    @Test
    void testErrorResponse_Structure() {
        CartDto request = new CartDto("PRD-00001", 1);

        when(jwtService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(false);
        when(jwtService.isTokenExpired(VALID_TOKEN)).thenReturn(true);

        ResponseEntity<?> result = cartController.addToCart(request, BEARER_TOKEN);

        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof ErrorResponse);
        
        ErrorResponse errorResponse = (ErrorResponse) result.getBody();
        assertNotNull(errorResponse.getStatus());
        assertNotNull(errorResponse.getCode());
        assertNotNull(errorResponse.getMessage());
        assertNotNull(errorResponse.getTimestamp());
        assertNotNull(errorResponse.getPath());
        assertEquals("UNAUTHORIZED", errorResponse.getStatus());
        assertEquals(401, errorResponse.getCode());
        assertEquals("/api/cart/add", errorResponse.getPath());
    }

    @Test
    void testErrorResponse_PathCorrect() {
        CartDto request = new CartDto("PRD-00001", 1);

        when(jwtService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(false);
        when(jwtService.isTokenExpired(VALID_TOKEN)).thenReturn(true);

        ResponseEntity<?> addResult = cartController.addToCart(request, BEARER_TOKEN);
        ErrorResponse addError = (ErrorResponse) addResult.getBody();
        assertEquals("/api/cart/add", addError.getPath());

        ResponseEntity<?> viewResult = cartController.viewCart(BEARER_TOKEN);
        ErrorResponse viewError = (ErrorResponse) viewResult.getBody();
        assertEquals("/api/cart/view", viewError.getPath());

        ResponseEntity<?> clearResult = cartController.clearCart(BEARER_TOKEN);
        ErrorResponse clearError = (ErrorResponse) clearResult.getBody();
        assertEquals("/api/cart/clear", clearError.getPath());
    }
}

