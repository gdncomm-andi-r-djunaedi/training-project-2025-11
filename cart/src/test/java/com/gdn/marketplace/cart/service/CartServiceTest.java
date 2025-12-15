package com.gdn.marketplace.cart.service;

import com.gdn.marketplace.cart.entity.Cart;
import com.gdn.marketplace.cart.entity.CartItem;
import com.gdn.marketplace.cart.repository.CartRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @InjectMocks
    private CartService service;

    @Mock
    private CartRepository repository;

    @Test
    void addToCart() {
        String username = "user";
        CartItem item = new CartItem("1", "Phone", 1, new BigDecimal("1000"));
        Cart cart = new Cart();
        cart.setUsername(username);
        cart.getItems().add(item);
        cart.setTotalPrice(new BigDecimal("1000"));

        when(repository.findByUsername(username)).thenReturn(Optional.of(new Cart()));
        when(repository.save(any(Cart.class))).thenReturn(cart);

        Cart updatedCart = service.addToCart(username, item);
        assertNotNull(updatedCart);
        assertEquals(1, updatedCart.getItems().size());
        assertEquals(new BigDecimal("1000"), updatedCart.getTotalPrice());
    }

    @Test
    void getCart() {
        String username = "user";
        Cart cart = new Cart();
        cart.setUsername(username);
        when(repository.findByUsername(username)).thenReturn(Optional.of(cart));

        Cart foundCart = service.getCart(username);
        assertNotNull(foundCart);
        assertEquals(username, foundCart.getUsername());
    }
}
