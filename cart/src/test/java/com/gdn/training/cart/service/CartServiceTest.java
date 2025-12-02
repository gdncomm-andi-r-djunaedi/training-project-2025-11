package com.gdn.training.cart.service;

import com.gdn.training.cart.client.ProductClient;
import com.gdn.training.cart.dto.ProductDTO;
import com.gdn.training.cart.entity.Cart;
import com.gdn.training.cart.entity.CartItem;
import com.gdn.training.cart.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    private static final String USER_ID = "user-1";
    private static final String PRODUCT_ID = "product-1";

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private CartService cartService;

    private ProductDTO productDTO;

    @BeforeEach
    void setUp() {
        productDTO = ProductDTO.builder()
                .id(PRODUCT_ID)
                .name("Gadget Prime")
                .description("flagship gadget")
                .price(BigDecimal.valueOf(499.99))
                .quantity(Integer.MAX_VALUE)
                .imageUrl("https://example.com/gadget-1.jpg")
                .build();
    }

    @Test
    void getCartReturnsExistingCart() {
        Cart existing = buildCart(List.of());
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existing));

        Cart result = cartService.getCart(USER_ID);

        assertThat(result).isSameAs(existing);
        verify(cartRepository).findByUserId(USER_ID);
        verify(cartRepository, never()).save(any());
    }

    @Test
    void getCartCreatesCartWhenMissing() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart toSave = invocation.getArgument(0);
            toSave.setId("generated-id");
            return toSave;
        });

        Cart result = cartService.getCart(USER_ID);

        assertThat(result.getId()).isEqualTo("generated-id");
        assertThat(result.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getItems()).isEmpty();
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void addToCartAddsNewItemWithProductMetadata() {
        Cart emptyCart = buildCart(new ArrayList<>());
        when(productClient.getProductById(PRODUCT_ID)).thenReturn(productDTO);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(emptyCart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartItem request = CartItem.builder()
                .productId(PRODUCT_ID)
                .productName("ignored")
                .price(BigDecimal.ONE)
                .quantity(2)
                .build();

        Cart updated = cartService.addToCart(USER_ID, request);

        assertThat(updated.getItems()).hasSize(1);
        CartItem savedItem = updated.getItems().getFirst();
        assertThat(savedItem.getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(savedItem.getProductName()).isEqualTo(productDTO.getName());
        assertThat(savedItem.getPrice()).isEqualTo(productDTO.getPrice());
        assertThat(savedItem.getImageUrl()).isEqualTo(productDTO.getImageUrl());
        assertThat(savedItem.getQuantity()).isEqualTo(2);
    }

    @Test
    void addToCartStacksQuantityWhenItemExists() {
        CartItem existingItem = CartItem.builder()
                .productId(PRODUCT_ID)
                .quantity(3)
                .productName("old name")
                .price(BigDecimal.TEN)
                .imageUrl("old-image")
                .build();
        Cart cart = buildCart(new ArrayList<>(List.of(existingItem)));

        when(productClient.getProductById(PRODUCT_ID)).thenReturn(productDTO);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartItem request = CartItem.builder()
                .productId(PRODUCT_ID)
                .quantity(2)
                .build();

        Cart updated = cartService.addToCart(USER_ID, request);

        assertThat(updated.getItems()).hasSize(1);
        CartItem savedItem = updated.getItems().getFirst();
        assertThat(savedItem.getQuantity()).isEqualTo(5);
        assertThat(savedItem.getProductName()).isEqualTo(productDTO.getName());
        assertThat(savedItem.getPrice()).isEqualTo(productDTO.getPrice());
    }

    @Test
    void addToCartThrowsWhenProductMissing() {
        when(productClient.getProductById(PRODUCT_ID)).thenReturn(null);

        CartItem request = CartItem.builder()
                .productId(PRODUCT_ID)
                .quantity(1)
                .build();

        assertThatThrownBy(() -> cartService.addToCart(USER_ID, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product not found");
        verify(cartRepository, never()).save(any());
    }

    @Test
    void removeFromCartDeletesMatchingItem() {
        CartItem keep = CartItem.builder().productId("other").quantity(1).build();
        CartItem remove = CartItem.builder().productId(PRODUCT_ID).quantity(1).build();
        Cart cart = buildCart(new ArrayList<>(List.of(keep, remove)));

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart updated = cartService.removeFromCart(USER_ID, PRODUCT_ID);

        assertThat(updated.getItems()).containsExactly(keep);
        verify(cartRepository).save(cart);
    }

    private Cart buildCart(List<CartItem> items) {
        return Cart.builder()
                .id("cart-1")
                .userId(USER_ID)
                .items(items)
                .build();
    }
}

