package com.gdn.training.cart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdn.training.cart.dto.AddToCartRequest;
import com.gdn.training.cart.dto.CartItemResponse;
import com.gdn.training.cart.dto.CartResponse;
import com.gdn.training.cart.service.CartService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
class CartControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private CartService cartService;

        @Test
        @WithMockUser(username = "testuser")
        void viewCart_Success() throws Exception {
                String username = "testuser";
                CartResponse response = new CartResponse();
                response.setId(UUID.randomUUID());
                response.setMemberId(username);

                List<CartItemResponse> items = new ArrayList<>();
                CartItemResponse item = new CartItemResponse();
                item.setId(UUID.randomUUID().toString());
                item.setProductId("SKU-000001");
                item.setProductName("Test Product");
                item.setPrice(10000.0);
                item.setQuantity(2);
                items.add(item);
                response.setCartItems(items);

                given(cartService.viewCart(username)).willReturn(response);

                mockMvc.perform(get("/api/carts/view-cart")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.memberId").value(username))
                                .andExpect(jsonPath("$.cartItems[0].productId").value("SKU-000001"))
                                .andExpect(jsonPath("$.cartItems[0].quantity").value(2));
        }

        @Test
        @WithMockUser(username = "testuser")
        void addToCart_Success() throws Exception {
                String username = "testuser";
                AddToCartRequest request = new AddToCartRequest();
                request.setProductId("SKU-000001");
                request.setQuantity(2);

                CartResponse cartResponse = new CartResponse();
                cartResponse.setId(UUID.randomUUID());
                cartResponse.setMemberId(username);

                CartItemResponse itemResponse = new CartItemResponse();
                itemResponse.setId(UUID.randomUUID().toString());
                itemResponse.setProductId("SKU-000001");
                itemResponse.setProductName("Test Product");
                itemResponse.setPrice(10000.0);
                itemResponse.setQuantity(2);
                cartResponse.setCartItems(List.of(itemResponse));

                given(cartService.addToCart(ArgumentMatchers.eq(username),
                                ArgumentMatchers.any(AddToCartRequest.class)))
                                .willReturn(cartResponse);

                mockMvc.perform(MockMvcRequestBuilders
                                .post("/api/carts/add-cart")
                                .with(SecurityMockMvcRequestPostProcessors
                                                .csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.memberId").value(username))
                                .andExpect(jsonPath("$.cartItems[0].productId").value("SKU-000001"))
                                .andExpect(jsonPath("$.cartItems[0].quantity").value(2));
        }

        @Test
        @WithMockUser(username = "testuser")
        void deleteProductFromCart_Success() throws Exception {
                String username = "testuser";
                String productId = "SKU-000001";
                CartResponse cartResponse = new CartResponse();
                cartResponse.setId(UUID.randomUUID());
                cartResponse.setMemberId(username);
                cartResponse.setCartItems(new ArrayList<>());

                given(cartService.deleteProductFromCart(username, productId)).willReturn(cartResponse);

                mockMvc.perform(MockMvcRequestBuilders
                                .delete("/api/carts/delete-from-cart/{productId}", productId)
                                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                                .csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.memberId").value(username))
                                .andExpect(jsonPath("$.cartItems").isEmpty());
        }
}
