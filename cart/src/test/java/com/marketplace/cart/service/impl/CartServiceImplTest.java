package com.marketplace.cart.service.impl;

import com.marketplace.cart.client.ProductServiceClient;
import com.marketplace.cart.client.dto.ProductResponse;
import com.marketplace.cart.dto.AddItemRequest;
import com.marketplace.cart.dto.CartResponse;
import com.marketplace.cart.entity.Cart;
import com.marketplace.cart.entity.CartItem;
import com.marketplace.cart.exception.CartNotFoundException;
import com.marketplace.cart.repository.CartRepository;
import com.marketplace.cart.util.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductServiceClient productServiceClient;

    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    void testGetCart_Success() {
        String userId = "user-123";
        Cart cart = Cart.builder()
                .userId(userId)
                .items(new ArrayList<>())
                .total(BigDecimal.ZERO)
                .build();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartResponse response = cartService.getCart(userId);

        assertNotNull(response);
        assertEquals(userId, response.getUserId());
    }

    @Test
    void testAddItem_SetQuantity() {
        String userId = "user-123";
        AddItemRequest request = new AddItemRequest();
        request.setProductId("PROD-123");
        request.setQuantity(5);

        Cart cart = Cart.builder()
                .userId(userId)
                .items(new ArrayList<>())
                .total(BigDecimal.ZERO)
                .build();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setPrice(BigDecimal.valueOf(100));
        ApiResponse<ProductResponse> apiResponse = ApiResponse.success(productResponse);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(productServiceClient.getProduct(anyString())).thenReturn(apiResponse);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartResponse response = cartService.addItem(userId, request, "set");

        assertNotNull(response);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void testAddItem_IncreaseQuantity() {
        String userId = "user-123";
        AddItemRequest request = new AddItemRequest();
        request.setProductId("PROD-123");
        request.setQuantity(2);

        CartItem existingItem = CartItem.builder()
                .productId("PROD-123")
                .quantity(3)
                .price(BigDecimal.valueOf(100))
                .build();

        Cart cart = Cart.builder()
                .userId(userId)
                .items(new ArrayList<>())
                .total(BigDecimal.ZERO)
                .build();
        cart.getItems().add(existingItem);

        ProductResponse productResponse = new ProductResponse();
        productResponse.setPrice(BigDecimal.valueOf(100));
        ApiResponse<ProductResponse> apiResponse = ApiResponse.success(productResponse);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(productServiceClient.getProduct(anyString())).thenReturn(apiResponse);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartResponse response = cartService.addItem(userId, request, "increase");

        assertNotNull(response);
        assertEquals(5, cart.getItems().get(0).getQuantity());
    }

    @Test
    void testRemoveItem_Success() {
        String userId = "user-123";
        String productId = "PROD-123";

        Cart cart = Cart.builder()
                .userId(userId)
                .items(new ArrayList<>())
                .total(BigDecimal.ZERO)
                .build();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartResponse response = cartService.removeItem(userId, productId);

        assertNotNull(response);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void testRemoveItem_CartNotFound() {
        String userId = "user-123";

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(CartNotFoundException.class, () -> cartService.removeItem(userId, "PROD-123"));
    }
}

