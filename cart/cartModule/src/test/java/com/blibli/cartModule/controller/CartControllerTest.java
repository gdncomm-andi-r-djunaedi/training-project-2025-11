package com.blibli.cartModule.controller;

import com.blibli.cartModule.dto.AddItemRequestDto;
import com.blibli.cartModule.dto.CartItemDto;
import com.blibli.cartModule.dto.CartResponseDto;
import com.blibli.cartModule.dto.RemoveItemDto;
import com.blibli.cartModule.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    private AddItemRequestDto addItemRequestDto;
    private RemoveItemDto removeItemDto;
    private CartResponseDto cartResponseDto;
    private CartItemDto cartItemDto;

    @BeforeEach
    void setUp() {
        addItemRequestDto = new AddItemRequestDto();
        addItemRequestDto.setProductId("PROD123");
        addItemRequestDto.setQuantity(2);

        removeItemDto = new RemoveItemDto();
        removeItemDto.setProductId("PROD123");
        removeItemDto.setQuantity(1);

        cartItemDto = new CartItemDto();
        cartItemDto.setProductId("PROD123");
        cartItemDto.setProductName("Test Product");
        cartItemDto.setProductImageUrl("http://example.com/image.jpg");
        cartItemDto.setProductPrice(new BigDecimal("100.00"));
        cartItemDto.setQuantity(2);
        cartItemDto.setItemPrice(new BigDecimal("200.00"));

        List<CartItemDto> items = new ArrayList<>();
        items.add(cartItemDto);

        cartResponseDto = new CartResponseDto();
        cartResponseDto.setMemberId(1L);
        cartResponseDto.setItems(items);
        cartResponseDto.setTotalPrice(new BigDecimal("200.00"));
    }

    @Test
    void testAddItem_Success() throws Exception {
        when(cartService.addItem(eq(1L), any(AddItemRequestDto.class))).thenReturn(cartResponseDto);

        mockMvc.perform(
                        post("/api/carts/addItems").header("Member-Id", "1").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(addItemRequestDto))).andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.value.memberId").value(1))
                .andExpect(jsonPath("$.value.items[0].productId").value("PROD123"))
                .andExpect(jsonPath("$.value.totalPrice").value(200.00));
    }

    @Test
    void testRemoveItem_Success() throws Exception {
        when(cartService.removeItem(eq(1L), any(RemoveItemDto.class))).thenReturn(cartResponseDto);

        mockMvc.perform(delete("/api/carts/removeItems").header("Member-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(removeItemDto))).andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.value.memberId").value(1));
    }

    @Test
    void testGetCart_Success() throws Exception {
        when(cartService.getCart(1L)).thenReturn(cartResponseDto);

        mockMvc.perform(get("/api/carts/getCart").header("Member-Id", "1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.value.memberId").value(1))
                .andExpect(jsonPath("$.value.items").isArray())
                .andExpect(jsonPath("$.value.totalPrice").value(200.00));
    }

    @Test
    void testClearCart_Success() throws Exception {

        mockMvc.perform(delete("/api/carts/clearCart").header("Member-Id", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.value").value("Cart cleared successfully"));
    }
}