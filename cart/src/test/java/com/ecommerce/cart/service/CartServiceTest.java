package com.ecommerce.cart.service;

import com.ecommerce.cart.entity.CartItem;
import com.ecommerce.cart.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private CartService cartService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addToCart_ShouldCreateNewItem_WhenNotExists() {
        when(cartRepository.findByUsernameAndProductId("user", "p1")).thenReturn(Optional.empty());

        cartService.addToCart("user", "p1", 1);

        verify(cartRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void addToCart_ShouldUpdateQuantity_WhenExists() {
        CartItem existing = new CartItem();
        existing.setQuantity(1);
        when(cartRepository.findByUsernameAndProductId("user", "p1")).thenReturn(Optional.of(existing));

        cartService.addToCart("user", "p1", 2);

        assertEquals(3, existing.getQuantity());
        verify(cartRepository, times(1)).save(existing);
    }

    @Test
    void viewCart_ShouldReturnList() {
        when(cartRepository.findByUsername("user")).thenReturn(Collections.emptyList());

        List<CartItem> result = cartService.viewCart("user");

        assertNotNull(result);
    }

    @Test
    void deleteFromCart_ShouldDelete_WhenAuthorized() {
        CartItem item = new CartItem();
        item.setId(1L);
        item.setUsername("user");
        when(cartRepository.findById(1L)).thenReturn(Optional.of(item));

        cartService.deleteFromCart("user", 1L);

        verify(cartRepository, times(1)).delete(item);
    }

    @Test
    void deleteFromCart_ShouldThrowException_WhenUnauthorized() {
        CartItem item = new CartItem();
        item.setId(1L);
        item.setUsername("other");
        when(cartRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(RuntimeException.class, () -> cartService.deleteFromCart("user", 1L));
    }
}
