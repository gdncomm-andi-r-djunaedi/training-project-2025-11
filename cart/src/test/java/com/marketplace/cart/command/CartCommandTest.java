package com.marketplace.cart.command;

import com.marketplace.cart.dto.AddToCartRequest;
import com.marketplace.cart.dto.request.AddToCartCommandRequest;
import com.marketplace.cart.dto.request.GetCartRequest;
import com.marketplace.cart.dto.request.RemoveFromCartRequest;
import com.marketplace.cart.dto.response.CartResponse;
import com.marketplace.cart.entity.Cart;
import com.marketplace.cart.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class CartCommandTest {

    @Mock
    private CartService cartService;

    private AddToCartCommand addToCartCommand;
    private GetCartCommand getCartCommand;
    private RemoveFromCartCommand removeFromCartCommand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        addToCartCommand = new AddToCartCommand(cartService);
        getCartCommand = new GetCartCommand(cartService);
        removeFromCartCommand = new RemoveFromCartCommand(cartService);
    }

    @Test
    void testAddToCartCommand() {
        UUID userId = UUID.randomUUID();
        AddToCartRequest addRequest = new AddToCartRequest();
        addRequest.setProductId("prod-1");
        addRequest.setProductName("Test Product");
        addRequest.setQuantity(1);
        addRequest.setPrice(BigDecimal.TEN);

        AddToCartCommandRequest request = AddToCartCommandRequest.builder()
                .userId(userId)
                .addToCartRequest(addRequest)
                .build();

        Cart expectedCart = Cart.builder().userId(userId).build();
        when(cartService.addToCart(userId, addRequest)).thenReturn(expectedCart);

        CartResponse result = addToCartCommand.execute(request);

        assertNotNull(result);
        verify(cartService, times(1)).addToCart(userId, addRequest);
    }

    @Test
    void testGetCartCommand() {
        UUID userId = UUID.randomUUID();
        GetCartRequest request = GetCartRequest.builder().userId(userId).build();

        Cart expectedCart = Cart.builder().userId(userId).build();
        when(cartService.getCart(userId)).thenReturn(expectedCart);

        CartResponse result = getCartCommand.execute(request);

        assertNotNull(result);
        verify(cartService, times(1)).getCart(userId);
    }

    @Test
    void testRemoveFromCartCommand() {
        UUID userId = UUID.randomUUID();
        String productId = "prod-1";

        RemoveFromCartRequest request = RemoveFromCartRequest.builder()
                .userId(userId)
                .productId(productId)
                .build();

        Cart expectedCart = Cart.builder().userId(userId).build();
        when(cartService.removeFromCart(userId, productId)).thenReturn(expectedCart);

        CartResponse result = removeFromCartCommand.execute(request);

        assertNotNull(result);
        verify(cartService, times(1)).removeFromCart(userId, productId);
    }
}
