package com.example.cartservice.controller;

import com.example.cartservice.dto.AddToCartRequest;
import com.example.cartservice.dto.CartDTO;
import com.example.cartservice.dto.CartItemDTO;
import com.example.cartservice.entity.Cart;
import com.example.cartservice.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @Test
    void getCart_shouldReturnCart() throws Exception {
        Long userId = 1L;
        CartItemDTO itemDTO = new CartItemDTO("p1", 2, "Product 1", "Desc", BigDecimal.TEN);
        CartDTO cartDTO = new CartDTO(userId, Collections.singletonList(itemDTO));

        when(cartService.getCart(userId)).thenReturn(cartDTO);

        mockMvc.perform(get("/api/cart")
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.items[0].productId").value("p1"));
    }

    @Test
    void addToCart_shouldReturnUpdatedCart() throws Exception {
        Long userId = 1L;
        Cart cart = new Cart(userId, new ArrayList<>());

        when(cartService.addToCart(eq(userId), any(AddToCartRequest.class))).thenReturn(cart);

        mockMvc.perform(post("/api/cart")
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\": \"p1\", \"quantity\": 1}"))
                .andExpect(status().isOk());
    }

    @Test
    void addToCart_shouldReturn400_whenProductNotFound() throws Exception {
        Long userId = 1L;
        when(cartService.addToCart(eq(userId), any(AddToCartRequest.class)))
                .thenThrow(new IllegalArgumentException("Product not found: 999"));

        mockMvc.perform(post("/api/cart")
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\": \"999\", \"quantity\": 1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Product not found: 999"));
    }

    @Test
    void removeFromCart_shouldReturnUpdatedCart() throws Exception {
        Long userId = 1L;
        Cart cart = new Cart(userId, new ArrayList<>());

        when(cartService.removeFromCart(userId, "p1")).thenReturn(cart);

        mockMvc.perform(delete("/api/cart/p1")
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
