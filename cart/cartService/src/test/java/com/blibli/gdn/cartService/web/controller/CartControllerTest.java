package com.blibli.gdn.cartService.web.controller;

import com.blibli.gdn.cartService.exception.CartNotFoundException;
import com.blibli.gdn.cartService.exception.ItemNotFoundInCartException;
import com.blibli.gdn.cartService.model.Cart;
import com.blibli.gdn.cartService.model.CartItem;
import com.blibli.gdn.cartService.service.CartService;
import com.blibli.gdn.cartService.web.model.AddToCartRequest;
import com.blibli.gdn.cartService.web.model.UpdateQuantityRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void addToCart_Success() throws Exception {
        AddToCartRequest request = new AddToCartRequest();
        request.setSku("SKU-123");
        request.setQty(1);

        Cart cart = Cart.builder()
                .memberId("member-123")
                .items(List.of(CartItem.builder().sku("SKU-123").qty(1).build()))
                .build();

        when(cartService.addToCart(anyString(), any(AddToCartRequest.class))).thenReturn(cart);

        mockMvc.perform(post("/api/v1/cart")
                        .header("X-User-Id", "member-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.cart.items[0].sku").value("SKU-123"));
    }

    @Test
    void getCart_Success() throws Exception {
        Cart cart = Cart.builder()
                .memberId("member-123")
                .items(new ArrayList<>())
                .build();

        when(cartService.getCart("member-123")).thenReturn(cart);

        mockMvc.perform(get("/api/v1/cart")
                        .header("X-User-Id", "member-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.memberId").value("member-123"));
    }

    @Test
    void updateQuantity_Success() throws Exception {
        UpdateQuantityRequest request = new UpdateQuantityRequest();
        request.setQty(5);

        Cart cart = Cart.builder()
                .memberId("member-123")
                .items(List.of(CartItem.builder().sku("SKU-123").qty(5).build()))
                .build();

        when(cartService.updateQuantity(eq("member-123"), eq("SKU-123"), any(UpdateQuantityRequest.class)))
                .thenReturn(cart);

        mockMvc.perform(put("/api/v1/cart/item/SKU-123")
                        .header("X-User-Id", "member-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.qty").value(5));
    }

    @Test
    void updateQuantity_CartNotFound() throws Exception {
        UpdateQuantityRequest request = new UpdateQuantityRequest();
        request.setQty(5);

        when(cartService.updateQuantity(eq("member-123"), eq("SKU-123"), any(UpdateQuantityRequest.class)))
                .thenThrow(new CartNotFoundException("member-123"));

        mockMvc.perform(put("/api/v1/cart/item/SKU-123")
                        .header("X-User-Id", "member-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Cart not found for member: member-123"));
    }

    @Test
    void removeItem_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/cart/SKU-123")
                        .header("X-User-Id", "member-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
