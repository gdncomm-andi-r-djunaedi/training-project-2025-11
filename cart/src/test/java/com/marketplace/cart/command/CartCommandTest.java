package com.marketplace.cart.command;

import com.marketplace.cart.dto.AddToCartRequest;
import com.marketplace.cart.entity.Cart;
import com.marketplace.cart.service.CartService;
import com.marketplace.common.command.CommandInvoker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CartCommandTest {

    @Mock
    private CartService cartService;

    private CommandInvoker invoker;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        invoker = new CommandInvoker();
    }

    @Test
    void testAddToCartCommand() {
        UUID userId = UUID.randomUUID();
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId("prod-1");
        request.setQuantity(1);
        request.setPrice(BigDecimal.TEN);

        Cart expectedCart = new Cart();
        when(cartService.addToCart(userId, request)).thenReturn(expectedCart);

        CartCommand command = new AddToCartCommand(cartService, userId, request);
        Cart result = invoker.executeCommand(command);

        assertEquals(expectedCart, result);
        verify(cartService, times(1)).addToCart(userId, request);
    }

    @Test
    void testRemoveFromCartCommand() {
        UUID userId = UUID.randomUUID();
        String productId = "prod-1";

        Cart expectedCart = new Cart();
        when(cartService.removeFromCart(userId, productId)).thenReturn(expectedCart);

        CartCommand command = new RemoveFromCartCommand(cartService, userId, productId);
        Cart result = invoker.executeCommand(command);

        assertEquals(expectedCart, result);
        verify(cartService, times(1)).removeFromCart(userId, productId);
    }
}
