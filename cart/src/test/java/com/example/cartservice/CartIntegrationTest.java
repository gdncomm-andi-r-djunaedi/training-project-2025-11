package com.example.cartservice;

import com.example.cartservice.dto.AddToCartRequest;
import com.example.cartservice.repository.CartRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class CartIntegrationTest extends AbstractIntegrationTest {

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
    void shouldAddToCart() throws Exception {
        AddToCartRequest request = new AddToCartRequest("p1", 1);

        mockMvc.perform(post("/api/cart/user1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user1"))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].productId").value("p1"));
    }

    @Test
    void shouldGetCart() throws Exception {
        // Add item first
        AddToCartRequest request = new AddToCartRequest("p1", 2);
        mockMvc.perform(post("/api/cart/user2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Get cart
        mockMvc.perform(get("/api/cart/user2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user2"))
                .andExpect(jsonPath("$.items", hasSize(1)));
    }

    @Test
    void shouldRemoveFromCart() throws Exception {
        // Add item first
        AddToCartRequest request = new AddToCartRequest("p1", 1);
        mockMvc.perform(post("/api/cart/user3")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Remove item
        mockMvc.perform(delete("/api/cart/user3/p1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)));
    }
}
