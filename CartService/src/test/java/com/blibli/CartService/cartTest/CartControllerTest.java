package com.blibli.CartService.cartTest;


import com.blibli.CartService.controller.CartController;
import com.blibli.CartService.dto.*;
import com.blibli.CartService.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void addToCart_success() throws Exception {
        AddToCartRequest request = new AddToCartRequest("SKU-1", 2);
        CartResponseDto response = new CartResponseDto();

        Mockito.when(cartService.addOrUpdateCart(eq("1"), any()))
                .thenReturn(response);

        mockMvc.perform(post("/cart/addToCart")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void viewCart_success() throws Exception {
        Mockito.when(cartService.viewCart("1"))
                .thenReturn(new CartResponseDto());

        mockMvc.perform(get("/cart/viewCart")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteItem_success() throws Exception {
        Mockito.doNothing()
                .when(cartService)
                .deleteItem("1", "SKU-1");

        mockMvc.perform(delete("/cart/deleteItem")
                        .header("X-User-Id", "1")
                        .param("productId", "SKU-1"))
                .andExpect(status().isOk());
    }
}
