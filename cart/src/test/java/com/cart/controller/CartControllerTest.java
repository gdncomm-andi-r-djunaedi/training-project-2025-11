package com.cart.controller;

import com.cart.dto.request.AddItemRequest;
import com.cart.dto.request.UpdateItemQuantityRequest;
import com.cart.entity.Cart;
import com.cart.entity.CartItem;
import com.cart.service.CartService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @Test
    void testGetCartByCustomerId() throws Exception {
        UUID customerId = UUID.randomUUID();
        Cart cart = new Cart();
        cart.setCustomerId(customerId);
        cart.setTotalPrice(BigDecimal.TEN);

        Mockito.when(cartService.getOrCreateCart(customerId)).thenReturn(cart);

        mockMvc.perform(get("/api/cart/" + customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(customerId.toString()));
    }

    @Test
    void testAddItem() throws Exception {
        UUID customerId = UUID.randomUUID();
        CartItem item = new CartItem();
        item.setProductId(UUID.randomUUID());
        item.setQuantity(2);

        Mockito.when(cartService.addItem(eq(customerId), any(AddItemRequest.class)))
                .thenReturn(item);

        mockMvc.perform(post("/api/cart/" + customerId + "/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": "11111111-1111-1111-1111-111111111111",
                                  "quantity": 2,
                                  "priceEach": 50.10,
                                  "productName": "Product A"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(2));
    }

    @Test
    void testUpdateItemQuantity() throws Exception {
        UUID customerId = UUID.randomUUID();
        CartItem item = new CartItem();
        item.setProductId(UUID.randomUUID());
        item.setQuantity(10);

        Mockito.when(cartService.updateItemQuantity(eq(customerId), any(UpdateItemQuantityRequest.class)))
                .thenReturn(item);

        mockMvc.perform(put("/api/cart/" + customerId + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": "11111111-1111-1111-1111-111111111111",
                                  "quantity": 10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(10));
    }

    @Test
    void testRemoveItem() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Cart cart = new Cart();
        cart.setCustomerId(customerId);

        Mockito.when(cartService.removeItem(customerId, productId))
                .thenReturn(cart);

        mockMvc.perform(delete("/api/cart/" + customerId + "/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "11111111-1111-1111-1111-111111111111",
                                  "productId": "%s"
                                }
                                """.formatted(productId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(customerId.toString()));
    }

    @Test
    void testClearCart() throws Exception {
        UUID customerId = UUID.randomUUID();

        mockMvc.perform(delete("/api/cart/" + customerId + "/clear"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cart cleared successfully"));

        Mockito.verify(cartService).clearCart(customerId);
    }
}
