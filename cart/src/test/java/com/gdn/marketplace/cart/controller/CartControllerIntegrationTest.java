package com.gdn.marketplace.cart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdn.marketplace.cart.dto.AddToCartRequest;
import com.gdn.marketplace.cart.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CartControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        cartRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "testuser")
    void addToCart_ShouldReturnCart() throws Exception {
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId("p1");
        request.setProductName("Test Product");
        request.setPrice(100.0);
        request.setQuantity(1);

        mockMvc.perform(post("/api/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productId").value("p1"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getCart_ShouldReturnCart() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isOk());
    }
}
