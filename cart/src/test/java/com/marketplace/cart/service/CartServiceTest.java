package com.marketplace.cart.service;

import com.marketplace.cart.cache.CartCache;
import com.marketplace.cart.client.ProductClient;
import com.marketplace.cart.dto.AddToCartRequest;
import com.marketplace.cart.dto.CartResponse;
import com.marketplace.cart.dto.ProductDTO;
import com.marketplace.cart.dto.UpdateCartItemRequest;
import com.marketplace.cart.mapper.CartMapper;
import com.marketplace.cart.repository.CartRepository;
import com.marketplace.common.exception.ResourceNotFoundException;
import com.marketplace.common.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartCacheService cartCacheService;

    @Mock
    private CartMapper cartMapper;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private CartService cartService;

    private CartCache cartCache;
    private CartResponse cartResponse;
    private AddToCartRequest addToCartRequest;
    private ProductDTO productDTO;

    @BeforeEach
    void setUp() {
        cartCache = CartCache.builder()
                .memberId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
                .items(new ArrayList<>())
                .totalAmount(BigDecimal.ZERO)
                .totalItems(0)
                .lastModified(LocalDateTime.now())
                .dirty(false)
                .build();

        cartResponse = CartResponse.builder()
                .memberId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
                .items(new ArrayList<>())
                .totalAmount(BigDecimal.ZERO)
                .totalItems(0)
                .build();

        addToCartRequest = AddToCartRequest.builder()
                .productId("prod-001")
                .quantity(2)
                .build();

        productDTO = ProductDTO.builder()
                .id("prod-001")
                .name("Test Product")
                .price(BigDecimal.valueOf(99.99))
                .images(List.of("https://example.com/image.jpg"))
                .active(true)
                .build();
    }

    @Nested
    @DisplayName("Get Cart Tests")
    class GetCartTests {

        @Test
        @DisplayName("Should return cart from cache")
        void shouldReturnCartFromCache() {
            // Given
            UUID memberId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
            when(cartCacheService.getCart(any(UUID.class))).thenReturn(Optional.of(cartCache));
            when(cartMapper.mapCacheToResponse(any(CartCache.class))).thenReturn(cartResponse);

            // When
            CartResponse result = cartService.getCart(memberId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getMemberId()).isEqualTo(memberId);
        }

        @Test
        @DisplayName("Should throw exception when member id is null")
        void shouldThrowExceptionWhenMemberIdNull() {
            // When/Then
            assertThatThrownBy(() -> cartService.getCart(null))
                    .isInstanceOf(UnauthorizedException.class);
        }
    }

    @Nested
    @DisplayName("Add To Cart Tests")
    class AddToCartTests {

        @Test
        @DisplayName("Should add new item to cart")
        void shouldAddNewItemToCart() {
            // Given
            UUID memberId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
            when(productClient.getProductByIdOrThrow(anyString())).thenReturn(productDTO);
            when(cartCacheService.getCart(any(UUID.class))).thenReturn(Optional.of(cartCache));
            when(cartMapper.mapCacheToResponse(any(CartCache.class))).thenReturn(cartResponse);

            // When
            CartResponse result = cartService.addToCart(memberId, addToCartRequest);

            // Then
            assertThat(result).isNotNull();
            verify(productClient).getProductByIdOrThrow("prod-001");
            verify(cartCacheService).saveCart(any(CartCache.class));
        }

        @Test
        @DisplayName("Should update quantity when product already in cart")
        void shouldUpdateQuantityWhenProductExists() {
            // Given
            CartCache.CartItemCache existingItem = CartCache.CartItemCache.builder()
                    .productId("prod-001")
                    .productName("Test Product")
                    .price(BigDecimal.valueOf(99.99))
                    .quantity(1)
                    .build();
            cartCache.getItems().add(existingItem);
            UUID memberId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

            when(productClient.getProductByIdOrThrow(anyString())).thenReturn(productDTO);
            when(cartCacheService.getCart(any(UUID.class))).thenReturn(Optional.of(cartCache));
            when(cartMapper.mapCacheToResponse(any(CartCache.class))).thenReturn(cartResponse);

            // When
            cartService.addToCart(memberId, addToCartRequest);

            // Then
            assertThat(existingItem.getQuantity()).isEqualTo(3); // 1 + 2
            verify(cartCacheService).saveCart(any(CartCache.class));
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void shouldThrowExceptionWhenProductNotFound() {
            // Given
            UUID memberId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
            when(productClient.getProductByIdOrThrow(anyString()))
                    .thenThrow(ResourceNotFoundException.of("Product", "prod-001"));

            // When/Then
            assertThatThrownBy(() -> cartService.addToCart(memberId, addToCartRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when product is inactive")
        void shouldThrowExceptionWhenProductInactive() {
            // Given
            UUID memberId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
            productDTO.setActive(false);
            when(productClient.getProductByIdOrThrow(anyString())).thenReturn(productDTO);

            // When/Then
            assertThatThrownBy(() -> cartService.addToCart(memberId, addToCartRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not available");
        }
    }

    @Nested
    @DisplayName("Update Cart Item Tests")
    class UpdateCartItemTests {

        @Test
        @DisplayName("Should update cart item quantity")
        void shouldUpdateCartItemQuantity() {
            // Given
            CartCache.CartItemCache item = CartCache.CartItemCache.builder()
                    .productId("prod-001")
                    .productName("Test Product")
                    .price(BigDecimal.valueOf(99.99))
                    .quantity(2)
                    .build();
            cartCache.getItems().add(item);

            UpdateCartItemRequest request = UpdateCartItemRequest.builder()
                    .quantity(5)
                    .build();
            UUID memberId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

            when(productClient.getProductByIdOrThrow(anyString())).thenReturn(productDTO);
            when(cartCacheService.getCart(any(UUID.class))).thenReturn(Optional.of(cartCache));
            when(cartMapper.mapCacheToResponse(any(CartCache.class))).thenReturn(cartResponse);

            // When
            cartService.updateCartItem(memberId, "prod-001", request);

            // Then
            assertThat(item.getQuantity()).isEqualTo(5);
            verify(cartCacheService).saveCart(any(CartCache.class));
        }

        @Test
        @DisplayName("Should throw exception when cart not found")
        void shouldThrowExceptionWhenCartNotFound() {
            // Given
            UpdateCartItemRequest request = UpdateCartItemRequest.builder()
                    .quantity(5)
                    .build();
            UUID memberId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

            when(productClient.getProductByIdOrThrow(anyString())).thenReturn(productDTO);
            when(cartCacheService.getCart(any(UUID.class))).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> cartService.updateCartItem(memberId, "prod-001", request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Remove From Cart Tests")
    class RemoveFromCartTests {

        @Test
        @DisplayName("Should remove item from cart")
        void shouldRemoveItemFromCart() {
            // Given
            CartCache.CartItemCache item = CartCache.CartItemCache.builder()
                    .productId("prod-001")
                    .productName("Test Product")
                    .price(BigDecimal.valueOf(99.99))
                    .quantity(2)
                    .build();
            cartCache.getItems().add(item);

            // Add another item so cart won't be empty
            CartCache.CartItemCache item2 = CartCache.CartItemCache.builder()
                    .productId("prod-002")
                    .productName("Test Product 2")
                    .price(BigDecimal.valueOf(49.99))
                    .quantity(1)
                    .build();
            cartCache.getItems().add(item2);
            UUID memberId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

            when(productClient.productExists(anyString())).thenReturn(true);
            when(cartCacheService.getCart(any(UUID.class))).thenReturn(Optional.of(cartCache));
            when(cartMapper.mapCacheToResponse(any(CartCache.class))).thenReturn(cartResponse);

            // When
            cartService.removeFromCart(memberId, "prod-001");

            // Then
            assertThat(cartCache.getItems()).hasSize(1);
            verify(cartCacheService).saveCart(any(CartCache.class));
        }

        @Test
        @DisplayName("Should throw exception when item not found")
        void shouldThrowExceptionWhenItemNotFound() {
            // Given
            UUID memberId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
            when(productClient.productExists(anyString())).thenReturn(true);
            when(cartCacheService.getCart(any(UUID.class))).thenReturn(Optional.of(cartCache));

            // When/Then
            assertThatThrownBy(() -> cartService.removeFromCart(memberId, "invalid-product"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
