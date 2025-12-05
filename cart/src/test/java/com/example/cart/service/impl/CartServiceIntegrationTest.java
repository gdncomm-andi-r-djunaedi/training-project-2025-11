package com.example.cart.service.impl;

import com.example.cart.client.ProductClient;
import com.example.cart.dto.AddToCartRequestDTO;
import com.example.cart.dto.CartResponseDTO;
import com.example.cart.dto.GetBulkProductResponseDTO;
import com.example.cart.entity.Cart;
import com.example.cart.repository.CartRepository;
import com.example.cart.service.CartService;
import com.example.cart.utils.APIResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class CartServiceIntegrationTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductClient productClient;

    @TestConfiguration
    static class TestConfig {

        @Bean
        public ProductClient productClient() {
            return Mockito.mock(ProductClient.class);
        }
    }

    private String userId;

    @BeforeEach
    void setUp() {
        userId = "testUser123";
        cartRepository.deleteAll();
    }

    @Test
    void addToCart_and_getCart_integrationFlow() {
        AddToCartRequestDTO addRequest = new AddToCartRequestDTO(1L, 2);
        String addResult = cartService.addToCartOrUpdateQuantity(userId, addRequest);
        assertEquals("product added to cart successfully", addResult);

        Optional<Cart> cartInDb = cartRepository.findById(userId);
        assertTrue(cartInDb.isPresent());
        assertEquals(1, cartInDb.get().getItems().size());

        GetBulkProductResponseDTO productDto = GetBulkProductResponseDTO.builder()
                .productId(1L)
                .title("Integration Test Product")
                .price(new BigDecimal("50.00"))
                .imageUrl("img.jpg")
                .markForDelete(false)
                .build();

        APIResponse<List<GetBulkProductResponseDTO>> apiResponse = new APIResponse<>();
        apiResponse.setData(Collections.singletonList(productDto));

        Mockito.when(productClient.fetchProductInBulk(Mockito.anyList()))
                .thenReturn(ResponseEntity.ok(apiResponse));

        CartResponseDTO cartResponse = cartService.getCart(userId);

        assertNotNull(cartResponse);
        assertEquals(userId, cartResponse.getUserId());
        assertEquals("Integration Test Product", cartResponse.getItems().get(0).getTitle());
        assertEquals(new BigDecimal("100.00"), cartResponse.getTotalPrice()); // 50 × 2
    }

    @Test
    void updateQuantity_integrationFlow() {
        cartService.addToCartOrUpdateQuantity(userId, new AddToCartRequestDTO(1L, 2));
        cartService.addToCartOrUpdateQuantity(userId, new AddToCartRequestDTO(1L, 3));

        Cart cart = cartRepository.findById(userId).orElseThrow();
        assertEquals(1, cart.getItems().size());
        assertEquals(5, cart.getItems().get(0).getQuantity());
    }

    @Test
    void removeItem_integrationFlow() {
        cartService.addToCartOrUpdateQuantity(userId, new AddToCartRequestDTO(1L, 2));
        cartService.addToCartOrUpdateQuantity(userId, new AddToCartRequestDTO(2L, 1));

        cartService.removeItemFromCart(userId, 1L);

        Cart cart = cartRepository.findById(userId).orElseThrow();
        assertEquals(1, cart.getItems().size());
        assertEquals(2L, cart.getItems().get(0).getProductId());
    }

    @Test
    void emptyCart_integrationFlow() {
        cartService.addToCartOrUpdateQuantity(userId, new AddToCartRequestDTO(1L, 2));

        cartService.emptyCart(userId);

        Cart cart = cartRepository.findById(userId).orElseThrow();
        assertTrue(cart.getItems().isEmpty());
    }
}

