package com.blibli.cart.service.impl;

import com.blibli.cart.client.ProductClient;
import com.blibli.cart.dto.*;
import com.blibli.cart.entity.Cart;
import com.blibli.cart.entity.CartItem;
import com.blibli.cart.exception.BadRequestException;
import com.blibli.cart.exception.ExternalServiceException;
import com.blibli.cart.exception.ResourceNotFoundException;
import com.blibli.cart.repository.CartRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Cart Service Implementation Tests")
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private CartServiceImpl cartService;

    private static final String USER_ID = "9df8a720-b423-4889-9665-9cec129dbf3f";
    private static final String PRODUCT_ID = "product-123";
    private static final String SKU = "SKU-123";
    private static final String CART_KEY = "cart:" + USER_ID;

    private ProductResponse productResponse;
    private AddToCartRequest addToCartRequest;
    private Cart cart;

    @BeforeEach
    void setUp() {
        productResponse = ProductResponse.builder()
                .id(PRODUCT_ID)
                .sku(SKU)
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .stockQuantity(100)
                .category("ELECTRONICS")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        addToCartRequest = AddToCartRequest.builder()
                .productId(PRODUCT_ID)
                .quantity(2)
                .build();

        cart = Cart.builder()
                .userId(USER_ID)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should add product to cart successfully when cart exists in Redis")
    void addToCart_Success_FromRedisCache() throws JsonProcessingException {
        // Given
        String cartJson = "{\"userId\":\"" + USER_ID + "\",\"items\":[]}";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(CART_KEY)).thenReturn(cartJson);
        when(objectMapper.readValue(cartJson, Cart.class)).thenReturn(cart);
        when(productClient.getProductById(PRODUCT_ID)).thenReturn(createApiResponse(productResponse));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(objectMapper.writeValueAsString(any(Cart.class))).thenReturn(cartJson);

        // When
        CartResponseDTO response = cartService.addToCart(USER_ID, addToCartRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(USER_ID);
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getTotalItems()).isEqualTo(2);
        verify(cartRepository).save(any(Cart.class));
        verify(redisTemplate.opsForValue()).set(eq(CART_KEY), anyString(), eq(7L), any());
    }

    @Test
    @DisplayName("Should add product to cart successfully when cart exists in MongoDB")
    void addToCart_Success_FromMongoDB() throws JsonProcessingException {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(CART_KEY)).thenReturn(null); // Cache miss
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(productClient.getProductById(PRODUCT_ID)).thenReturn(createApiResponse(productResponse));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(objectMapper.writeValueAsString(any(Cart.class))).thenReturn("{}");

        // When
        CartResponseDTO response = cartService.addToCart(USER_ID, addToCartRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(1);
        verify(cartRepository).findByUserId(USER_ID);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("Should create new cart when cart does not exist")
    void addToCart_Success_CreateNewCart() throws JsonProcessingException {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(CART_KEY)).thenReturn(null);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(productClient.getProductById(PRODUCT_ID)).thenReturn(createApiResponse(productResponse));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(objectMapper.writeValueAsString(any(Cart.class))).thenReturn("{}");

        // When
        CartResponseDTO response = cartService.addToCart(USER_ID, addToCartRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(1);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("Should update quantity when product already exists in cart")
    void addToCart_Success_UpdateExistingItem() throws JsonProcessingException {
        // Given
        CartItem existingItem = CartItem.builder()
                .productId(PRODUCT_ID)
                .sku(SKU)
                .name("Test Product")
                .price(new BigDecimal("99.99"))
                .quantity(3)
                .addedAt(LocalDateTime.now())
                .build();
        cart.getItems().add(existingItem);

        String cartJson = "{\"userId\":\"" + USER_ID + "\",\"items\":[{}]}";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(CART_KEY)).thenReturn(cartJson);
        when(objectMapper.readValue(cartJson, Cart.class)).thenReturn(cart);
        when(productClient.getProductById(PRODUCT_ID)).thenReturn(createApiResponse(productResponse));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(objectMapper.writeValueAsString(any(Cart.class))).thenReturn(cartJson);

        // When
        CartResponseDTO response = cartService.addToCart(USER_ID, addToCartRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getTotalItems()).isEqualTo(5); // 3 + 2
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException when productId is null")
    void addToCart_Failure_NullProductId() {
        // Given
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(null)
                .quantity(1)
                .build();

        // When/Then
        assertThatThrownBy(() -> cartService.addToCart(USER_ID, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Product ID is required");
    }

    @Test
    @DisplayName("Should throw BadRequestException when productId is empty")
    void addToCart_Failure_EmptyProductId() {
        // Given
        AddToCartRequest request = AddToCartRequest.builder()
                .productId("   ")
                .quantity(1)
                .build();

        // When/Then - Exception thrown before any service calls
        assertThatThrownBy(() -> cartService.addToCart(USER_ID, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Product ID is required");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when product does not exist")
    void addToCart_Failure_ProductNotFound() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(CART_KEY)).thenReturn(null);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(productClient.getProductById(PRODUCT_ID)).thenThrow(FeignException.NotFound.class);

        // When/Then
        assertThatThrownBy(() -> cartService.addToCart(USER_ID, addToCartRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");
        
        verify(productClient).getProductById(PRODUCT_ID);
    }

    @Test
    @DisplayName("Should throw BadRequestException when insufficient stock")
    void addToCart_Failure_InsufficientStock() throws JsonProcessingException {
        // Given
        productResponse.setStockQuantity(1);
        String cartJson = "{\"userId\":\"" + USER_ID + "\",\"items\":[]}";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(CART_KEY)).thenReturn(cartJson);
        when(objectMapper.readValue(cartJson, Cart.class)).thenReturn(cart);
        when(productClient.getProductById(PRODUCT_ID)).thenReturn(createApiResponse(productResponse));

        // When/Then
        assertThatThrownBy(() -> cartService.addToCart(USER_ID, addToCartRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient stock");
        
        verify(productClient).getProductById(PRODUCT_ID);
    }

    @Test
    @DisplayName("Should throw ExternalServiceException when product service fails")
    void addToCart_Failure_ProductServiceError() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(CART_KEY)).thenReturn(null);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(productClient.getProductById(PRODUCT_ID)).thenThrow(FeignException.InternalServerError.class);

        // When/Then
        assertThatThrownBy(() -> cartService.addToCart(USER_ID, addToCartRequest))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessageContaining("Failed to fetch product details");
        
        verify(productClient).getProductById(PRODUCT_ID);
    }

    @Test
    @DisplayName("Should get cart successfully from Redis cache")
    void getCart_Success_FromRedis() throws JsonProcessingException {
        // Given
        String cartJson = "{\"userId\":\"" + USER_ID + "\",\"items\":[]}";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(CART_KEY)).thenReturn(cartJson);
        when(objectMapper.readValue(cartJson, Cart.class)).thenReturn(cart);

        // When
        CartResponseDTO response = cartService.getCarts(USER_ID);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(USER_ID);
        verify(valueOperations).get(CART_KEY);
        verify(cartRepository, never()).findByUserId(USER_ID);
    }

    @Test
    @DisplayName("Should get cart successfully from MongoDB when cache miss")
    void getCart_Success_FromMongoDB() throws JsonProcessingException {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(CART_KEY)).thenReturn(null);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(objectMapper.writeValueAsString(any(Cart.class))).thenReturn("{}");

        // When
        CartResponseDTO response = cartService.getCarts(USER_ID);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(USER_ID);
        verify(cartRepository).findByUserId(USER_ID);
    }

    @Test
    @DisplayName("Should remove product from cart successfully")
    void removeFromCart_Success() throws JsonProcessingException {
        // Given
        CartItem item = CartItem.builder()
                .productId(PRODUCT_ID)
                .sku(SKU)
                .name("Test Product")
                .price(new BigDecimal("99.99"))
                .quantity(2)
                .addedAt(LocalDateTime.now())
                .build();
        cart.getItems().add(item);

        String cartJson = "{\"userId\":\"" + USER_ID + "\",\"items\":[{}]}";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(CART_KEY)).thenReturn(cartJson);
        when(objectMapper.readValue(cartJson, Cart.class)).thenReturn(cart);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(objectMapper.writeValueAsString(any(Cart.class))).thenReturn(cartJson);

        // When
        CartResponseDTO response = cartService.removeItemFromCart(USER_ID, PRODUCT_ID);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getItems()).isEmpty();
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException when product not found in cart")
    void removeFromCart_Failure_ProductNotFound() throws JsonProcessingException {
        // Given
        String cartJson = "{\"userId\":\"" + USER_ID + "\",\"items\":[]}";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(CART_KEY)).thenReturn(cartJson);
        when(objectMapper.readValue(cartJson, Cart.class)).thenReturn(cart);

        // When/Then
        assertThatThrownBy(() -> cartService.removeItemFromCart(USER_ID, PRODUCT_ID))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Product not found in cart");
    }

    @Test
    @DisplayName("Should clear cart successfully")
    void clearCart_Success() {
        // When
        cartService.clearCart(USER_ID);

        // Then
        verify(cartRepository).deleteByUserId(USER_ID);
        verify(redisTemplate).delete(CART_KEY);
    }

    @Test
    @DisplayName("Should handle Redis deserialization error gracefully")
    void getCartFromCache_RedisDeserializationError() throws JsonProcessingException {
        // Given
        String cartJson = "invalid json";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(CART_KEY)).thenReturn(cartJson);
        when(objectMapper.readValue(cartJson, Cart.class)).thenThrow(new JsonProcessingException("Invalid JSON") {});
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(objectMapper.writeValueAsString(any(Cart.class))).thenReturn("{}");

        // When
        CartResponseDTO response = cartService.getCarts(USER_ID);

        // Then
        assertThat(response).isNotNull();
        verify(cartRepository).findByUserId(USER_ID);
    }

    private ApiResponse<ProductResponse> createApiResponse(ProductResponse product) {
        ApiResponse<ProductResponse> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setData(product);
        return response;
    }
}

