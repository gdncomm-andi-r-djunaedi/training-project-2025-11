package com.gdn.training.cart.service;

import com.gdn.training.cart.dto.AddToCartRequest;
import com.gdn.training.cart.dto.CartResponse;
import com.gdn.training.cart.dto.ProductDetailResponse;
import com.gdn.training.cart.entity.Cart;
import com.gdn.training.cart.entity.CartItem;
import com.gdn.training.cart.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CartServiceImpl cartService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(cartService, "productBaseUrl", "http://localhost:8081");
    }

    @Test
    void viewCart_CartExists() {
        String username = "testuser";
        UUID cartId = UUID.randomUUID();
        Cart cart = new Cart();
        cart.setId(cartId);
        cart.setMemberId(username);
        cart.setCartItems(new ArrayList<>());

        CartItem item = new CartItem();
        item.setId(UUID.randomUUID());
        item.setProductId("SKU-000001");
        item.setProductName("Test Product");
        item.setPrice(10000.0);
        item.setQuantity(2);
        item.setCart(cart);
        cart.getCartItems().add(item);

        when(cartRepository.findByMemberId(username)).thenReturn(Optional.of(cart));

        CartResponse response = cartService.viewCart(username);

        assertNotNull(response);
        assertEquals(username, response.getMemberId());
        assertEquals(cartId, response.getId());
        assertEquals(1, response.getCartItems().size());
        assertEquals("SKU-000001", response.getCartItems().get(0).getProductId());
        assertEquals(2, response.getCartItems().get(0).getQuantity());
        assertEquals(10000.0, response.getCartItems().get(0).getPrice());

        verify(cartRepository, times(1)).findByMemberId(username);
    }

    @Test
    void viewCart_CartNotFound() {
        String username = "testuser";
        when(cartRepository.findByMemberId(username)).thenReturn(Optional.empty());

        CartResponse response = cartService.viewCart(username);

        assertNotNull(response);
        assertEquals(username, response.getMemberId());
        assertNull(response.getId());
        assertNotNull(response.getCartItems());
        assertTrue(response.getCartItems().isEmpty());

        verify(cartRepository, times(1)).findByMemberId(username);
    }

    @Test
    void addToCart_HappyFlow() {
        String username = "testuser";
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId("SKU-000001");
        request.setQuantity(2);

        ProductDetailResponse productData = new ProductDetailResponse();
        productData.setProductId("SKU-000001");
        productData.setProductName("Test Product");
        productData.setPrice(10000.0);

        ResponseEntity<ProductDetailResponse> productResponse = ResponseEntity.ok(productData);
        when(restTemplate.getForEntity(anyString(), eq(ProductDetailResponse.class))).thenReturn(productResponse);

        Cart cart = new Cart();
        cart.setMemberId(username);
        cart.setCartItems(new ArrayList<>());
        when(cartRepository.findByMemberId(username)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartResponse updatedCart = cartService.addToCart(username, request);

        assertNotNull(updatedCart);
        assertEquals(1, updatedCart.getCartItems().size());
        assertEquals("SKU-000001", updatedCart.getCartItems().get(0).getProductId());
        assertEquals("Test Product", updatedCart.getCartItems().get(0).getProductName());
        assertEquals(10000.0, updatedCart.getCartItems().get(0).getPrice());
        assertEquals(2, updatedCart.getCartItems().get(0).getQuantity());

        verify(restTemplate, times(1)).getForEntity(anyString(), eq(ProductDetailResponse.class));
        verify(cartRepository, times(1)).findByMemberId(username);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void deleteProductFromCart_Success() {
        String username = "testuser";
        String productId = "SKU-000001";
        Cart cart = new Cart();
        cart.setMemberId(username);
        cart.setCartItems(new ArrayList<>());

        CartItem item = new CartItem();
        item.setProductId(productId);
        item.setCart(cart);
        cart.getCartItems().add(item);

        when(cartRepository.findByMemberId(username)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartResponse updatedCart = cartService.deleteProductFromCart(username, productId);

        assertNotNull(updatedCart);
        assertTrue(updatedCart.getCartItems().isEmpty());
        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    void deleteProductFromCart_ItemNotFound() {
        String username = "testuser";
        String productId = "SKU-000001";
        Cart cart = new Cart();
        cart.setMemberId(username);
        cart.setCartItems(new ArrayList<>());

        when(cartRepository.findByMemberId(username)).thenReturn(Optional.of(cart));

        CartResponse updatedCart = cartService.deleteProductFromCart(username, productId);

        assertNotNull(updatedCart);
        assertTrue(updatedCart.getCartItems().isEmpty());
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void deleteProductFromCart_CartNotFound() {
        String username = "testuser";
        String productId = "SKU-000001";
        when(cartRepository.findByMemberId(username)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> cartService.deleteProductFromCart(username, productId));
    }
}
