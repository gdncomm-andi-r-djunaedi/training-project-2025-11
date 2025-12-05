package com.sc.cartservice;

import com.sc.cartservice.client.ProductFeign;
import com.sc.cartservice.config.MongoTestContainerConfig;
import com.sc.cartservice.dto.CartResponseDto;
import com.sc.cartservice.model.Cart;
import com.sc.cartservice.model.CartItem;
import com.sc.cartservice.repository.CartItemRepository;
import com.sc.cartservice.repository.CartRepository;
import com.sc.cartservice.service.CartService;
import com.sc.productservice.dto.ProductDto;
import com.sc.utilsservice.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CartServiceTest extends MongoTestContainerConfig {

    @Autowired
    private CartService cartService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @MockBean
    private ProductFeign productFeign;

    private ProductDto fakeProduct;

    @BeforeEach
    void cleanDb() {
        cartRepository.deleteAll();
        cartItemRepository.deleteAll();
        fakeProduct = new ProductDto("p1", "P1", 1000.0);
    }

    @Test
    void testCreateCart() {
        ApiResponse<String> response = cartService.createCart("M1");

        assertThat(response.isSuccess()).isTrue();
        Cart saved = cartRepository.findByMemberId("M1");
        assertThat(saved).isNotNull();
        assertThat(saved.getTotalPrice()).isEqualTo(0.0);
    }

    @Test
    void testCreateCart_NullMember() {
        ApiResponse<String> response = cartService.createCart(null);
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorCode()).isEqualTo("INVALID_CREDENTIALS");
    }

    @Test
    void testAddToCart_Success() {
        Cart cart = new Cart(null, "M1", new ArrayList<>(), 0.0);
        cartRepository.save(cart);

        when(productFeign.getProduct("P1"))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(fakeProduct)));

        ApiResponse<CartResponseDto> response = cartService.addToCart("M1", "P1");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getTotalPrice()).isEqualTo(1000.0);
        assertThat(response.getData().getCartItemDtos().size()).isEqualTo(1);
    }

    @Test
    void testAddToCart_ProductNotFound() {
        Cart cart = new Cart(null, "M1", new ArrayList<>(), 0.0);
        cartRepository.save(cart);

        when(productFeign.getProduct("XX"))
                .thenReturn(ResponseEntity.ok(ApiResponse.failure("NOT_FOUND", "Product missing")));

        ApiResponse<CartResponseDto> response = cartService.addToCart("M1", "XX");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorCode()).isEqualTo("PRODUCT_NOT_FOUND");
    }

    @Test
    void testAddToCart_ExistingItemIncreaseQuantity() {
        CartItem item = new CartItem(null, "P1", "Phone", 1000.0, 1);
        List<CartItem> items = new ArrayList<>();
        items.add(item);

        Cart cart = new Cart(null, "M1", items, 1000.0);
        cartRepository.save(cart);

        when(productFeign.getProduct("P1"))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(fakeProduct)));

        ApiResponse<CartResponseDto> response = cartService.addToCart("M1", "P1");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getTotalPrice()).isEqualTo(2000.0);
        assertThat(response.getData().getCartItemDtos().get(0).getQuantity()).isEqualTo(2);
    }

    @Test
    void testGetCart() {
        Cart cart = new Cart(null, "M10", new ArrayList<>(), 0.0);
        cartRepository.save(cart);

        ApiResponse<CartResponseDto> response = cartService.getCart("M10");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getMemberId()).isEqualTo("M10");
    }

    @Test
    void testGetCart_NullMember() {
        ApiResponse<CartResponseDto> response = cartService.getCart(null);
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorCode()).isEqualTo("INVALID_CREDENTIALS");
    }

    @Test
    void testRemoveFromCart_Success() {
        CartItem item = new CartItem(null, "P1", "Phone", 1000.0, 1);

        List<CartItem> items = new ArrayList<>();
        items.add(item);

        Cart cart = new Cart(null, "M1", items, 1000.0);
        cartRepository.save(cart);

        ApiResponse<CartResponseDto> response = cartService.removeFromCart("M1", "P1");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getCartItemDtos().size()).isEqualTo(0);
        assertThat(response.getData().getTotalPrice()).isEqualTo(0.0);
    }

    @Test
    void testRemoveFromCart_ItemNotFound() {
        Cart cart = new Cart(null, "M1", new ArrayList<>(), 0.0);
        cartRepository.save(cart);

        ApiResponse<CartResponseDto> response = cartService.removeFromCart("M1", "ABC");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorCode()).isEqualTo("ITEM_NOT_FOUND");
    }

    @Test
    void testUpdateQuantity_Increase() {
        CartItem item = new CartItem("null", "P1", "test", 1000.0, 1);
        List<CartItem> items = new ArrayList<>();
        items.add(item);

        Cart cart = new Cart(null, "M5", items, 1000.0);
        cartRepository.save(cart);

        ApiResponse<CartResponseDto> response = cartService.updateCartItemQuantity("M5", "P1", 1);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getTotalPrice()).isEqualTo(2000.0);
        assertThat(response.getData().getCartItemDtos().get(0).getQuantity()).isEqualTo(2);
    }

    @Test
    void testUpdateQuantity_RemoveWhenZero() {
        CartItem item = new CartItem(null, "P1", "name", 1000.0, 1);
        Cart cart = new Cart(null, "M1", List.of(item), 1000.0);
        cartRepository.save(cart);

        ApiResponse<CartResponseDto> response = cartService.updateCartItemQuantity("M1", "P1", -1);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getCartItemDtos().size()).isEqualTo(0);
    }

    @Test
    void testUpdateQuantity_ItemNotFound() {
        Cart cart = new Cart(null, "M1", new ArrayList<>(), 0.0);
        cartRepository.save(cart);

        ApiResponse<CartResponseDto> response = cartService.updateCartItemQuantity("M1", "X", 1);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorCode()).isEqualTo("ITEM_NOT_FOUND");
    }
}
