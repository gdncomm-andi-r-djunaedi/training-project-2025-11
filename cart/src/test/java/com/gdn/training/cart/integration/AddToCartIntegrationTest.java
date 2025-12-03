package com.gdn.training.cart.integration;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdn.training.cart.dto.AddToCartRequest;
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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AddToCartIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RestTemplate restTemplate;

    private String validToken;

    @BeforeEach
    void setUp() {
        cartRepository.deleteAll();
        
        // Generate a valid token
        validToken = JWT.create()
                .withSubject("testuser")
                .withIssuer("testingIssuer")
                .withExpiresAt(new Date(new Date().getTime() + 60 * 60 * 1000))
                .withJWTId(UUID.randomUUID().toString())
                .sign(Algorithm.HMAC256("testkey"));
    }

    @Test
    void addToCart_HappyFlow() throws Exception {
        // Mock Product Service Response
        Map<String, Object> productResponse = new HashMap<>();
        productResponse.put("status", "success");
        productResponse.put("product_id", "SKU-000001");
        productResponse.put("product_name", "Test Product");
        productResponse.put("price", 10000);
        productResponse.put("description", "Desc");

        Mockito.when(restTemplate.getForEntity(anyString(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(productResponse, HttpStatus.OK));

        AddToCartRequest request = new AddToCartRequest();
        request.setProductId("SKU-000001");
        request.setQuantity(2);

        mockMvc.perform(post("/api/carts/add")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value("testuser"))
                .andExpect(jsonPath("$.cartItems", hasSize(1)))
                .andExpect(jsonPath("$.cartItems[0].productId").value("p-1"))
                .andExpect(jsonPath("$.cartItems[0].quantity").value(2));
    }

    @Test
    void addToCart_InvalidProduct() {
        // Mock Product Service Response for 404
        Mockito.when(restTemplate.getForEntity(anyString(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        AddToCartRequest request = new AddToCartRequest();
        request.setProductId("invalid-sku");
        request.setQuantity(1);

        jakarta.servlet.ServletException exception = org.junit.jupiter.api.Assertions.assertThrows(jakarta.servlet.ServletException.class, () -> {
            mockMvc.perform(post("/api/carts/add")
                            .header("Authorization", "Bearer " + validToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)));
        });

        org.junit.jupiter.api.Assertions.assertTrue(exception.getCause() instanceof RuntimeException);
        org.junit.jupiter.api.Assertions.assertEquals("Product not found", exception.getCause().getMessage());
    }
}
