package com.blibli.gdn.cartService.service.impl;

import com.blibli.gdn.cartService.client.ProductServiceClient;
import com.blibli.gdn.cartService.client.dto.ProductDTO;
import com.blibli.gdn.cartService.client.dto.VariantDTO;
import com.blibli.gdn.cartService.exception.CartNotFoundException;
import com.blibli.gdn.cartService.exception.InvalidQuantityException;
import com.blibli.gdn.cartService.exception.ItemNotFoundInCartException;
import com.blibli.gdn.cartService.model.Cart;
import com.blibli.gdn.cartService.model.CartItem;
import com.blibli.gdn.cartService.repository.CartRepository;
import com.blibli.gdn.cartService.web.model.AddToCartRequest;
import com.blibli.gdn.cartService.web.model.UpdateQuantityRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductServiceClient productServiceClient;

    @InjectMocks
    private CartServiceImpl cartService;

    private static final String MEMBER_ID = "member-123";
    private static final String SKU = "SKU-123";
    private static final String PRODUCT_ID = "PROD-123";

    private ProductDTO productDTO;
    private VariantDTO variantDTO;
    private Cart cart;

    @BeforeEach
    void setUp() {
        productDTO = ProductDTO.builder()
                .productId(PRODUCT_ID)
                .name("Test Product")
                .build();

        variantDTO = VariantDTO.builder()
                .sku(SKU)
                .price(new BigDecimal("100.00"))
                .color("Red")
                .size("M")
                .build();

        cart = Cart.builder()
                .memberId(MEMBER_ID)
                .items(new ArrayList<>())
                .currency("USD")
                .build();
    }

    @Test
    void addToCart_NewItem_Success() {
        AddToCartRequest request = new AddToCartRequest();
        request.setSku(SKU);
        request.setQty(1);

        when(productServiceClient.getProductBySku(SKU)).thenReturn(productDTO);
        when(productServiceClient.getVariantBySku(productDTO, SKU)).thenReturn(variantDTO);
        when(cartRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart updatedCart = cartService.addToCart(MEMBER_ID, request);

        assertNotNull(updatedCart);
        assertEquals(1, updatedCart.getItems().size());
        assertEquals(SKU, updatedCart.getItems().get(0).getSku());
        assertEquals(1, updatedCart.getItems().get(0).getQty());
        assertEquals(new BigDecimal("100.00"), updatedCart.getTotalValue());
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void addToCart_ExistingItem_Success() {
        CartItem existingItem = CartItem.builder()
                .sku(SKU)
                .qty(1)
                .price(new BigDecimal("100.00"))
                .build();
        cart.getItems().add(existingItem);

        AddToCartRequest request = new AddToCartRequest();
        request.setSku(SKU);
        request.setQty(2);

        when(productServiceClient.getProductBySku(SKU)).thenReturn(productDTO);
        when(productServiceClient.getVariantBySku(productDTO, SKU)).thenReturn(variantDTO);
        when(cartRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart updatedCart = cartService.addToCart(MEMBER_ID, request);

        assertNotNull(updatedCart);
        assertEquals(1, updatedCart.getItems().size());
        assertEquals(3, updatedCart.getItems().get(0).getQty());
        assertEquals(new BigDecimal("300.00"), updatedCart.getTotalValue());
    }

    @Test
    void updateQuantity_Success() {
        CartItem existingItem = CartItem.builder()
                .sku(SKU)
                .qty(1)
                .price(new BigDecimal("100.00"))
                .build();
        cart.getItems().add(existingItem);

        UpdateQuantityRequest request = new UpdateQuantityRequest();
        request.setQty(5);

        when(cartRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart updatedCart = cartService.updateQuantity(MEMBER_ID, SKU, request);

        assertEquals(5, updatedCart.getItems().get(0).getQty());
        assertEquals(new BigDecimal("500.00"), updatedCart.getTotalValue());
    }

    @Test
    void updateQuantity_CartNotFound() {
        UpdateQuantityRequest request = new UpdateQuantityRequest();
        request.setQty(5);

        when(cartRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.empty());

        assertThrows(CartNotFoundException.class, () ->
                cartService.updateQuantity(MEMBER_ID, SKU, request));
    }

    @Test
    void updateQuantity_ItemNotFound() {
        UpdateQuantityRequest request = new UpdateQuantityRequest();
        request.setQty(5);

        when(cartRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(cart));

        assertThrows(ItemNotFoundInCartException.class, () ->
                cartService.updateQuantity(MEMBER_ID, SKU, request));
    }

    @Test
    void removeItem_Success() {
        CartItem existingItem = CartItem.builder()
                .sku(SKU)
                .qty(1)
                .price(new BigDecimal("100.00"))
                .build();
        cart.getItems().add(existingItem);

        when(cartRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(cart));

        cartService.removeItem(MEMBER_ID, SKU);

        assertTrue(cart.getItems().isEmpty());
        assertEquals(BigDecimal.ZERO, cart.getTotalValue());
        verify(cartRepository).save(cart);
    }

    @Test
    void removeItem_CartNotFound() {
        when(cartRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.empty());

        assertThrows(CartNotFoundException.class, () ->
                cartService.removeItem(MEMBER_ID, SKU));
    }

    @Test
    void mergeCarts_Success() {
        String guestCartId = "guest-123";
        Cart guestCart = Cart.builder()
                .memberId(guestCartId)
                .items(new ArrayList<>(List.of(
                        CartItem.builder().sku(SKU).qty(1).price(new BigDecimal("100.00")).build()
                )))
                .build();

        when(cartRepository.findByMemberId(guestCartId)).thenReturn(Optional.of(guestCart));
        when(cartRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(cart));

        cartService.mergeCarts(guestCartId, MEMBER_ID);

        assertEquals(1, cart.getItems().size());
        assertEquals(SKU, cart.getItems().get(0).getSku());
        verify(cartRepository).deleteByMemberId(guestCartId);
        verify(cartRepository).save(cart);
    }
}
