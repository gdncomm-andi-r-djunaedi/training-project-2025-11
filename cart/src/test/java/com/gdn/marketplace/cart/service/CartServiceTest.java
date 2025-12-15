package com.gdn.marketplace.cart.service;

import com.gdn.marketplace.cart.dto.AddToCartRequest;
import com.gdn.marketplace.cart.entity.Cart;
import com.gdn.marketplace.cart.entity.CartItem;
import com.gdn.marketplace.cart.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private CartService cartService;

    private Cart cart;

    @BeforeEach
    void setUp() {
        cart = new Cart();
        cart.setId(1L);
        cart.setUsername("testuser");
        cart.setItems(new ArrayList<>());
    }

    @Test
    void addToCart_NewItem_Success() {
        when(cartRepository.findByUsername(anyString())).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        AddToCartRequest request = new AddToCartRequest();
        request.setProductId("p1");
        request.setProductName("Product 1");
        request.setPrice(10.0);
        request.setQuantity(1);

        Cart result = cartService.addToCart("testuser", request);

        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals("p1", result.getItems().get(0).getProductId());
    }

    @Test
    void addToCart_ExistingItem_IncrementsQuantity() {
        CartItem item = new CartItem();
        item.setProductId("p1");
        item.setQuantity(1);
        cart.getItems().add(item);

        when(cartRepository.findByUsername(anyString())).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        AddToCartRequest request = new AddToCartRequest();
        request.setProductId("p1");
        request.setQuantity(2);

        Cart result = cartService.addToCart("testuser", request);

        assertEquals(1, result.getItems().size());
        assertEquals(3, result.getItems().get(0).getQuantity());
    }

    @Test
    void removeFromCart_Success() {
        CartItem item = new CartItem();
        item.setProductId("p1");
        cart.getItems().add(item);

        when(cartRepository.findByUsername(anyString())).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        Cart result = cartService.removeFromCart("testuser", "p1");

        assertTrue(result.getItems().isEmpty());
    }
}
