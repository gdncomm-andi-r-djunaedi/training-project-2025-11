package com.gdn.training.cart.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdn.training.cart.dto.AddToCartRequest;
import com.gdn.training.cart.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LoggedOutTokenIntegrationTest {

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
    void addToCart_WithoutToken_ShouldReturnForbidden() throws Exception {
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId("SKU-000001");
        request.setQuantity(2);

        mockMvc.perform(post("/api/carts/add-cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void viewCart_WithoutToken_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/carts/view-cart"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteFromCart_WithoutToken_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/carts/delete-from-cart/SKU-000001"))
                .andExpect(status().isForbidden());
    }
}
