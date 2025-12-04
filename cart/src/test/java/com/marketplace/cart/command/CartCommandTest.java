package com.marketplace.cart.command;

import com.marketplace.cart.command.impl.AddToCartCommandImpl;
import com.marketplace.cart.command.impl.GetCartCommandImpl;
import com.marketplace.cart.command.impl.RemoveFromCartCommandImpl;
import com.marketplace.cart.dto.AddToCartRequest;
import com.marketplace.cart.dto.request.AddToCartCommandRequest;
import com.marketplace.cart.dto.request.GetCartRequest;
import com.marketplace.cart.dto.request.RemoveFromCartRequest;
import com.marketplace.cart.dto.response.CartResponse;
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
class CartCommandTest {

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private AddToCartCommandImpl addToCartCommand;

    @InjectMocks
    private GetCartCommandImpl getCartCommand;

    @InjectMocks
    private RemoveFromCartCommandImpl removeFromCartCommand;

    @Test
    void addToCart_NewCart_Success() {
        UUID userId = UUID.randomUUID();
        AddToCartRequest addRequest = new AddToCartRequest();
        addRequest.setProductId("p1");
        addRequest.setProductName("Product 1");
        addRequest.setPrice(BigDecimal.TEN);
        addRequest.setQuantity(1);

        AddToCartCommandRequest request = AddToCartCommandRequest.builder()
                .userId(userId)
                .addToCartRequest(addRequest)
                .build();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartResponse result = addToCartCommand.execute(request);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(1, result.getItems().size());
        assertEquals("p1", result.getItems().get(0).getProductId());
    }

    @Test
    void addToCart_ExistingCart_NewItem_Success() {
        UUID userId = UUID.randomUUID();
        AddToCartRequest addRequest = new AddToCartRequest();
        addRequest.setProductId("p2");
        addRequest.setProductName("Product 2");
        addRequest.setPrice(BigDecimal.TEN);
        addRequest.setQuantity(1);

        AddToCartCommandRequest request = AddToCartCommandRequest.builder()
                .userId(userId)
                .addToCartRequest(addRequest)
                .build();

        Cart existingCart = Cart.builder()
                .userId(userId)
                .items(new ArrayList<>())
                .build();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(existingCart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartResponse result = addToCartCommand.execute(request);

        assertEquals(1, result.getItems().size());
        assertEquals("p2", result.getItems().get(0).getProductId());
    }

    @Test
    void addToCart_ExistingItem_UpdateQuantity_Success() {
        UUID userId = UUID.randomUUID();
        AddToCartRequest addRequest = new AddToCartRequest();
        addRequest.setProductId("p1");
        addRequest.setQuantity(2);

        AddToCartCommandRequest request = AddToCartCommandRequest.builder()
                .userId(userId)
                .addToCartRequest(addRequest)
                .build();

        CartItem existingItem = CartItem.builder()
                .productId("p1")
                .quantity(1)
                .price(BigDecimal.TEN)
                .build();
        Cart existingCart = Cart.builder()
                .userId(userId)
                .items(new ArrayList<>())
                .build();
        existingCart.addItem(existingItem);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(existingCart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartResponse result = addToCartCommand.execute(request);

        assertEquals(1, result.getItems().size());
        assertEquals(3, result.getItems().get(0).getQuantity());
    }

    @Test
    void getCart_Success() {
        UUID userId = UUID.randomUUID();
        GetCartRequest request = GetCartRequest.builder().userId(userId).build();

        Cart cart = Cart.builder().userId(userId).build();
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

        CartResponse result = getCartCommand.execute(request);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
    }

    @Test
    void getCart_NotFound_ReturnsEmptyCart() {
        UUID userId = UUID.randomUUID();
        GetCartRequest request = GetCartRequest.builder().userId(userId).build();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

        CartResponse result = getCartCommand.execute(request);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void removeFromCart_Success() {
        UUID userId = UUID.randomUUID();
        String productId = "p1";

        RemoveFromCartRequest request = RemoveFromCartRequest.builder()
                .userId(userId)
                .productId(productId)
                .build();

        CartItem item = CartItem.builder().productId(productId).price(BigDecimal.TEN).quantity(1).build();
        Cart cart = Cart.builder().userId(userId).items(new ArrayList<>()).build();
        cart.addItem(item);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartResponse result = removeFromCartCommand.execute(request);

        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void removeFromCart_CartNotFound_ThrowsException() {
        UUID userId = UUID.randomUUID();
        String productId = "p1";

        RemoveFromCartRequest request = RemoveFromCartRequest.builder()
                .userId(userId)
                .productId(productId)
                .build();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(CartNotFoundException.class, () -> removeFromCartCommand.execute(request));
    }
}
