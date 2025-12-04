package com.marketplace.cart.service;

import com.marketplace.cart.dto.AddToCartRequest;
import com.marketplace.cart.entity.Cart;
import com.marketplace.cart.entity.CartItem;
import com.marketplace.cart.exception.CartNotFoundException;
import com.marketplace.cart.repository.CartRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    void addToCart_NewCart_Success() {
        UUID userId = UUID.randomUUID();
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId("p1");
        request.setProductName("Product 1");
        request.setPrice(BigDecimal.TEN);
        request.setQuantity(1);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart result = cartService.addToCart(userId, request);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(1, result.getItems().size());
        assertEquals("p1", result.getItems().get(0).getProductId());
    }

    @Test
    void addToCart_ExistingCart_NewItem_Success() {
        UUID userId = UUID.randomUUID();
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId("p2");
        request.setProductName("Product 2");
        request.setPrice(BigDecimal.TEN);
        request.setQuantity(1);

        Cart existingCart = Cart.builder()
                .userId(userId)
                .items(new ArrayList<>())
                .build();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(existingCart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart result = cartService.addToCart(userId, request);

        assertEquals(1, result.getItems().size());
        assertEquals("p2", result.getItems().get(0).getProductId());
    }

    @Test
    void addToCart_ExistingItem_UpdateQuantity_Success() {
        UUID userId = UUID.randomUUID();
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId("p1");
        request.setQuantity(2);

        CartItem existingItem = CartItem.builder()
                .productId("p1")
                .quantity(1)
                .build();
        Cart existingCart = Cart.builder()
                .userId(userId)
                .items(new ArrayList<>())
                .build();
        existingCart.addItem(existingItem);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(existingCart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart result = cartService.addToCart(userId, request);

        assertEquals(1, result.getItems().size());
        assertEquals(3, result.getItems().get(0).getQuantity());
    }

    @Test
    void removeFromCart_Success() {
        UUID userId = UUID.randomUUID();
        String productId = "p1";

        CartItem item = CartItem.builder().productId(productId).build();
        Cart cart = Cart.builder().userId(userId).items(new ArrayList<>()).build();
        cart.addItem(item);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart result = cartService.removeFromCart(userId, productId);

        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void removeFromCart_CartNotFound_ThrowsException() {
        UUID userId = UUID.randomUUID();
        String productId = "p1";

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(CartNotFoundException.class, () -> cartService.removeFromCart(userId, productId));
    }
}
