package com.marketplace.cart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.cart.client.ProductServiceClient;
import com.marketplace.cart.dto.AddToCartRequest;
import com.marketplace.cart.dto.response.ProductDetailsResponse;
import com.marketplace.cart.entity.Cart;
import com.marketplace.cart.entity.CartItem;
import com.marketplace.cart.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CartControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CartRepository cartRepository;

    @MockBean
    private ProductServiceClient productServiceClient;

    private static final String USER_ID_HEADER = "X-User-Id";
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        cartRepository.deleteAll();
        testUserId = UUID.randomUUID();
    }

    @Test
    void addToCart_NewCart_ReturnsCreated() throws Exception {
        String productId = "prod-123";
        
        when(productServiceClient.getProductById(productId)).thenReturn(
                ProductDetailsResponse.builder()
                        .id(productId)
                        .name("Test Product")
                        .price(new BigDecimal("49.99"))
                        .build()
        );

        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(productId);
        request.setQuantity(2);

        mockMvc.perform(post("/api/cart/add")
                        .header(USER_ID_HEADER, testUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Item added to cart"))
                .andExpect(jsonPath("$.data.userId").value(testUserId.toString()))
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].productId").value(productId))
                .andExpect(jsonPath("$.data.items[0].productName").value("Test Product"))
                .andExpect(jsonPath("$.data.items[0].quantity").value(2));
    }

    @Test
    void addToCart_ExistingCart_AddsItem() throws Exception {
        // Create existing cart
        Cart existingCart = Cart.builder()
                .userId(testUserId)
                .items(new ArrayList<>())
                .build();
        existingCart.addItem(CartItem.builder()
                .productId("existing-prod")
                .productName("Existing Product")
                .price(new BigDecimal("19.99"))
                .quantity(1)
                .build());
        cartRepository.save(existingCart);

        String newProductId = "new-prod";
        when(productServiceClient.getProductById(newProductId)).thenReturn(
                ProductDetailsResponse.builder()
                        .id(newProductId)
                        .name("New Product")
                        .price(new BigDecimal("29.99"))
                        .build()
        );

        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(newProductId);
        request.setQuantity(1);

        mockMvc.perform(post("/api/cart/add")
                        .header(USER_ID_HEADER, testUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.items", hasSize(2)));
    }

    @Test
    void addToCart_ExistingItem_UpdatesQuantity() throws Exception {
        String productId = "prod-123";
        
        // Create existing cart with same product
        Cart existingCart = Cart.builder()
                .userId(testUserId)
                .items(new ArrayList<>())
                .build();
        existingCart.addItem(CartItem.builder()
                .productId(productId)
                .productName("Test Product")
                .price(new BigDecimal("49.99"))
                .quantity(2)
                .build());
        cartRepository.save(existingCart);

        when(productServiceClient.getProductById(productId)).thenReturn(
                ProductDetailsResponse.builder()
                        .id(productId)
                        .name("Test Product")
                        .price(new BigDecimal("49.99"))
                        .build()
        );

        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(productId);
        request.setQuantity(3);

        mockMvc.perform(post("/api/cart/add")
                        .header(USER_ID_HEADER, testUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].quantity").value(5));  // 2 + 3
    }

    @Test
    void addToCart_MissingUserIdHeader_ReturnsError() throws Exception {
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId("prod-123");
        request.setQuantity(1);

        // Missing required header causes internal error (header is required by controller)
        mockMvc.perform(post("/api/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void getCart_ExistingCart_ReturnsCart() throws Exception {
        Cart cart = Cart.builder()
                .userId(testUserId)
                .items(new ArrayList<>())
                .build();
        cart.addItem(CartItem.builder()
                .productId("prod-1")
                .productName("Product 1")
                .price(new BigDecimal("19.99"))
                .quantity(2)
                .build());
        cart.addItem(CartItem.builder()
                .productId("prod-2")
                .productName("Product 2")
                .price(new BigDecimal("29.99"))
                .quantity(1)
                .build());
        cartRepository.save(cart);

        mockMvc.perform(get("/api/cart")
                        .header(USER_ID_HEADER, testUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cart retrieved successfully"))
                .andExpect(jsonPath("$.data.userId").value(testUserId.toString()))
                .andExpect(jsonPath("$.data.items", hasSize(2)));
    }

    @Test
    void getCart_NonExistingCart_ReturnsEmptyCart() throws Exception {
        mockMvc.perform(get("/api/cart")
                        .header(USER_ID_HEADER, testUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(testUserId.toString()))
                .andExpect(jsonPath("$.data.items", hasSize(0)));
    }

    @Test
    void removeFromCart_ExistingItem_RemovesItem() throws Exception {
        String productIdToRemove = "prod-1";
        
        Cart cart = Cart.builder()
                .userId(testUserId)
                .items(new ArrayList<>())
                .build();
        cart.addItem(CartItem.builder()
                .productId(productIdToRemove)
                .productName("Product 1")
                .price(new BigDecimal("19.99"))
                .quantity(2)
                .build());
        cart.addItem(CartItem.builder()
                .productId("prod-2")
                .productName("Product 2")
                .price(new BigDecimal("29.99"))
                .quantity(1)
                .build());
        cartRepository.save(cart);

        mockMvc.perform(delete("/api/cart/{productId}", productIdToRemove)
                        .header(USER_ID_HEADER, testUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Item removed from cart"))
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].productId").value("prod-2"));
    }

    @Test
    void removeFromCart_NonExistingCart_ReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/api/cart/{productId}", "prod-123")
                        .header(USER_ID_HEADER, testUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("CART_NOT_FOUND"));
    }

    @Test
    void removeFromCart_LastItem_ReturnsEmptyCart() throws Exception {
        String productId = "prod-1";
        
        Cart cart = Cart.builder()
                .userId(testUserId)
                .items(new ArrayList<>())
                .build();
        cart.addItem(CartItem.builder()
                .productId(productId)
                .productName("Product 1")
                .price(new BigDecimal("19.99"))
                .quantity(1)
                .build());
        cartRepository.save(cart);

        mockMvc.perform(delete("/api/cart/{productId}", productId)
                        .header(USER_ID_HEADER, testUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(0)));
    }

    @Test
    void addToCart_InvalidQuantity_ReturnsBadRequest() throws Exception {
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId("prod-123");
        request.setQuantity(0);  // Invalid quantity

        mockMvc.perform(post("/api/cart/add")
                        .header(USER_ID_HEADER, testUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addToCart_MissingProductId_ReturnsBadRequest() throws Exception {
        AddToCartRequest request = new AddToCartRequest();
        request.setQuantity(1);
        // productId is missing

        mockMvc.perform(post("/api/cart/add")
                        .header(USER_ID_HEADER, testUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}

