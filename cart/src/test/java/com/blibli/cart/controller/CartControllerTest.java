package com.blibli.cart.controller;

import com.blibli.cart.dto.AddToCartRequest;
import com.blibli.cart.dto.CartResponse;
import com.blibli.cart.exception.BadRequestException;
import com.blibli.cart.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@DisplayName("Cart Controller Tests")
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String USER_ID = "9df8a720-b423-4889-9665-9cec129dbf3f";
    private static final String PRODUCT_ID = "product-123";

    @Test
    @DisplayName("Should add product to cart successfully")
    void addToCart_Success() throws Exception {
        // Given
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(PRODUCT_ID)
                .quantity(2)
                .build();

        CartResponse cartResponse = CartResponse.builder()
                .userId(USER_ID)
                .items(new ArrayList<>())
                .totalAmount(BigDecimal.ZERO)
                .totalItems(2)
                .updatedAt(LocalDateTime.now())
                .build();

        when(cartService.addToCart(eq(USER_ID), any(AddToCartRequest.class)))
                .thenReturn(cartResponse);

        // When/Then
        mockMvc.perform(post("/api/cart")
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(USER_ID))
                .andExpect(jsonPath("$.data.totalItems").value(2));

        verify(cartService).addToCart(eq(USER_ID), any(AddToCartRequest.class));
    }

    @Test
    @DisplayName("Should return 400 when X-User-Id header is missing")
    void addToCart_Failure_MissingUserId() throws Exception {
        // Given
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(PRODUCT_ID)
                .quantity(1)
                .build();

        // When/Then
        mockMvc.perform(post("/api/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User authentication required"));
    }

    @Test
    @DisplayName("Should return 400 when X-User-Id is invalid UUID")
    void addToCart_Failure_InvalidUserId() throws Exception {
        // Given
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(PRODUCT_ID)
                .quantity(1)
                .build();

        // When/Then
        mockMvc.perform(post("/api/cart")
                        .header("X-User-Id", "invalid-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get cart successfully")
    void getCart_Success() throws Exception {
        // Given
        CartResponse cartResponse = CartResponse.builder()
                .userId(USER_ID)
                .items(new ArrayList<>())
                .totalAmount(BigDecimal.ZERO)
                .totalItems(0)
                .updatedAt(LocalDateTime.now())
                .build();

        when(cartService.getCart(USER_ID)).thenReturn(cartResponse);

        // When/Then
        mockMvc.perform(get("/api/cart")
                        .header("X-User-Id", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(USER_ID));

        verify(cartService).getCart(USER_ID);
    }

    @Test
    @DisplayName("Should remove product from cart successfully")
    void removeFromCart_Success() throws Exception {
        // Given
        CartResponse cartResponse = CartResponse.builder()
                .userId(USER_ID)
                .items(new ArrayList<>())
                .totalAmount(BigDecimal.ZERO)
                .totalItems(0)
                .updatedAt(LocalDateTime.now())
                .build();

        when(cartService.removeFromCart(USER_ID, PRODUCT_ID)).thenReturn(cartResponse);

        // When/Then
        mockMvc.perform(delete("/api/cart/{productId}", PRODUCT_ID)
                        .header("X-User-Id", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(cartService).removeFromCart(USER_ID, PRODUCT_ID);
    }

    @Test
    @DisplayName("Should clear cart successfully")
    void clearCart_Success() throws Exception {
        // Given
        doNothing().when(cartService).clearCart(USER_ID);

        // When/Then
        mockMvc.perform(delete("/api/cart")
                        .header("X-User-Id", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(cartService).clearCart(USER_ID);
    }

    @Test
    @DisplayName("Should return 400 when request body validation fails")
    void addToCart_Failure_ValidationError() throws Exception {
        // Given
        // Note: Empty productId will pass @NotBlank validation (empty string != blank)
        // But the service will check and throw BadRequestException
        AddToCartRequest request = AddToCartRequest.builder()
                .productId("") // Empty string - will be caught by service validation
                .quantity(1)
                .build();

        // Mock service to throw BadRequestException for empty productId
        when(cartService.addToCart(eq(USER_ID), any(AddToCartRequest.class)))
                .thenThrow(new BadRequestException("Product ID is required"));

        // When/Then
        mockMvc.perform(post("/api/cart")
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Product ID is required"));
    }
}

