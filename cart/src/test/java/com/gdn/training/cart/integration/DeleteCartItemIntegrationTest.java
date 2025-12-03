package com.gdn.training.cart.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdn.training.cart.dto.AddToCartRequest;
import com.gdn.training.cart.repository.CartItemRepository;
import com.gdn.training.cart.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DeleteCartItemIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RestTemplate restTemplate;

    private String validToken;

    @BeforeEach
    void setUp() {
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();

        validToken = com.auth0.jwt.JWT.create()
                .withSubject("testuser")
                .sign(com.auth0.jwt.algorithms.Algorithm.HMAC256("testkey"));
    }

    @Test
    void deleteProduct_HappyFlow() throws Exception {
        // 1. Prepare Data: Add item to cart
        Mockito.when(restTemplate.getForEntity(anyString(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(Map.of(
                        "product_id", "SKU-000001",
                        "product_name", "Test Product",
                        "price", 10000), HttpStatus.OK));

        AddToCartRequest request = new AddToCartRequest();
        request.setProductId("SKU-000001");
        request.setQuantity(2);

        mockMvc.perform(post("/api/carts/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // 2. Delete Product
        mockMvc.perform(delete("/api/carts/p-1")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartItems").isEmpty());
    }

    @Test
    void deleteProduct_ItemNotFound() throws Exception {
        // 1. Prepare Data: Add item to cart
        Mockito.when(restTemplate.getForEntity(anyString(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(Map.of(
                        "product_id", "SKU-000001",
                        "product_name", "Test Product",
                        "price", 10000), HttpStatus.OK));

        AddToCartRequest request = new AddToCartRequest();
        request.setProductId("p-1");
        request.setQuantity(2);

        mockMvc.perform(post("/api/carts/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // 2. Delete Non-existent Product
        mockMvc.perform(delete("/api/carts/SKU-invalid")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk());
    }
}
