package com.gdn.training.cart.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import com.gdn.training.cart.model.dto.ProductDetailDTO;
import com.gdn.training.cart.model.request.AddToCartRequest;
import com.gdn.training.cart.model.response.CartResponse;

@ExtendWith(MockitoExtension.class)
public class CartServiceTests {
    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private org.springframework.web.client.RestTemplate restTemplate;

    @InjectMocks
    private CartService cartService;

    @Test
    void getCartByMemberId_Success() {
        String userEmail = "test@example.com";
        String cartKey = "cart:" + userEmail;
        String productKey = cartKey + ":product";

        Map<Object, Object> cartItems = new HashMap<>();
        cartItems.put("PROD001", 2);
        cartItems.put("PROD002", 1);

        Map<Object, Object> productDetails = new HashMap<>();

        ProductDetailDTO product1 = ProductDetailDTO.builder()
                .id("PROD001")
                .name("Product 1")
                .price(100.0)
                .build();

        ProductDetailDTO product2 = ProductDetailDTO.builder()
                .id("PROD002")
                .name("Product 2")
                .price(200.0)
                .build();

        productDetails.put("PROD001", product1);
        productDetails.put("PROD002", product2);

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries(cartKey)).thenReturn(cartItems);
        when(hashOperations.entries(productKey)).thenReturn(productDetails);

        CartResponse response = cartService.getCart(userEmail);

        assertNotNull(response);
        assertEquals("OK", response.getMessage());
        assertEquals(userEmail, response.getUserEmail());
        assertEquals(2, response.getItemCount());
        assertEquals(2, response.getCartItems().size());
        assertEquals(400.0, response.getTotal());
    }

    @Test
    void getCartByMemberIdEmpty_Success() {
        String userEmail = "test@example.com";
        String cartKey = "cart:" + userEmail;

        Map<Object, Object> emptyCart = new HashMap<>();

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries(cartKey)).thenReturn(emptyCart);

        CartResponse response = cartService.getCart(userEmail);

        assertNotNull(response);
        assertEquals("OK", response.getMessage());
        assertEquals(userEmail, response.getUserEmail());
        assertEquals(0, response.getItemCount());
        assertEquals(0, response.getCartItems().size());
        assertEquals(0.0, response.getTotal());
    }

    @Test
    void getCartByMemberId_ThrowsException() {
        String userEmail = "test@example.com";
        String cartKey = "cart:" + userEmail;
        String productKey = cartKey + ":product";

        Map<Object, Object> cartItems = new HashMap<>();
        cartItems.put("PROD001", 2);

        Map<Object, Object> productDetails = new HashMap<>();

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries(cartKey)).thenReturn(cartItems);
        when(hashOperations.entries(productKey)).thenReturn(productDetails);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cartService.getCart(userEmail));

        assertEquals("Product not found", exception.getMessage());
    }

    @Test
    void addToCart_NewProduct_Success() {
        String userEmail = "test@example.com";
        String cartKey = "cart:" + userEmail;
        String productKey = cartKey + ":product";
        String productId = "PROD001";
        Integer quantity = 2;

        AddToCartRequest request = new AddToCartRequest(productId, quantity);

        ProductDetailDTO productDetail = ProductDetailDTO.builder()
                .id(productId)
                .name("Product 1")
                .price(100.0)
                .build();

        Map<Object, Object> cartItems = new HashMap<>();
        cartItems.put(productId, quantity);

        Map<Object, Object> productDetails = new HashMap<>();
        productDetails.put(productId, productDetail);

        when(restTemplate.getForObject(
                "http://localhost:8082/products/detail/" + productId,
                ProductDetailDTO.class)).thenReturn(productDetail);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.get(cartKey, productId)).thenReturn(null);
        when(hashOperations.entries(cartKey)).thenReturn(cartItems);
        when(hashOperations.entries(productKey)).thenReturn(productDetails);

        CartResponse response = cartService.addToCart(userEmail, request);

        assertNotNull(response);
        assertEquals("Product added to cart", response.getMessage());
        assertEquals(userEmail, response.getUserEmail());
    }

    @Test
    void addToCart_ExistingProduct_Success() {
        String userEmail = "test@example.com";
        String cartKey = "cart:" + userEmail;
        String productKey = cartKey + ":product";
        String productId = "PROD001";
        Integer existingQuantity = 3;
        Integer addQuantity = 2;
        Integer totalQuantity = 5;

        AddToCartRequest request = new AddToCartRequest(productId, addQuantity);

        ProductDetailDTO productDetail = ProductDetailDTO.builder()
                .id(productId)
                .name("Product 1")
                .price(100.0)
                .build();

        Map<Object, Object> cartItems = new HashMap<>();
        cartItems.put(productId, totalQuantity);

        Map<Object, Object> productDetails = new HashMap<>();
        productDetails.put(productId, productDetail);

        when(restTemplate.getForObject(
                "http://localhost:8082/products/detail/" + productId,
                ProductDetailDTO.class)).thenReturn(productDetail);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.get(cartKey, productId)).thenReturn(existingQuantity);
        when(hashOperations.entries(cartKey)).thenReturn(cartItems);
        when(hashOperations.entries(productKey)).thenReturn(productDetails);

        CartResponse response = cartService.addToCart(userEmail, request);

        assertNotNull(response);
        assertEquals("Product added to cart", response.getMessage());
        assertEquals(userEmail, response.getUserEmail());
    }

    @Test
    void removeFromCart_LastItem_Success() {
        String userEmail = "test@example.com";
        String cartKey = "cart:" + userEmail;
        String productId = "PROD001";

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.size(cartKey)).thenReturn(0L);

        CartResponse response = cartService.removeFromCart(userEmail, productId);

        assertNotNull(response);
        assertEquals(userEmail, response.getUserEmail());
        assertEquals(0, response.getItemCount());
        assertEquals(0, response.getCartItems().size());
        assertEquals(0.0, response.getTotal());
    }

    @Test
    void removeFromCart_OneOfMultipleItems_Success() {
        String userEmail = "test@example.com";
        String cartKey = "cart:" + userEmail;
        String productKey = cartKey + ":product";
        String productIdToRemove = "PROD001";

        Map<Object, Object> remainingCartItems = new HashMap<>();
        remainingCartItems.put("PROD002", 1);

        ProductDetailDTO product2 = ProductDetailDTO.builder()
                .id("PROD002")
                .name("Product 2")
                .price(200.0)
                .build();

        Map<Object, Object> remainingProductDetails = new HashMap<>();
        remainingProductDetails.put("PROD002", product2);

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.size(cartKey)).thenReturn(1L);
        when(hashOperations.entries(cartKey)).thenReturn(remainingCartItems);
        when(hashOperations.entries(productKey)).thenReturn(remainingProductDetails);

        CartResponse response = cartService.removeFromCart(userEmail, productIdToRemove);

        assertNotNull(response);
        assertEquals("Product removed from cart", response.getMessage());
        assertEquals(userEmail, response.getUserEmail());
        assertEquals(1, response.getItemCount());
    }

    @Test
    void addToCart_FetchProductDetailFails_ThrowsException() {
        String userEmail = "test@example.com";
        String productId = "PROD001";
        Integer quantity = 2;

        AddToCartRequest request = new AddToCartRequest(productId, quantity);

        when(restTemplate.getForObject(
                "http://localhost:8082/products/detail/" + productId,
                ProductDetailDTO.class)).thenThrow(new RuntimeException("API Error"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> cartService.addToCart(userEmail, request));

        assertEquals("Failed to fetch product detail", exception.getMessage());
    }
}
