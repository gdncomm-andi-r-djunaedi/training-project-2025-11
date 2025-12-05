package com.microservice.cart;

import com.microservice.cart.dto.AddToCartRequestDto;
import com.microservice.cart.dto.CartDto;
import com.microservice.cart.dto.ProductResponseDto;
import com.microservice.cart.entity.Cart;
import com.microservice.cart.client.ProductFeign;
import com.microservice.cart.repository.CartRepository;
import com.microservice.cart.service.impl.CartServiceImpl;
import com.microservice.cart.wrapper.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Cart Service Basic Tests")
public class CartApplicationTests {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductFeign productFeign;

    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    @DisplayName("Test 1: getCart should return empty cart when cart not found")
    void testGetCart_CartNotFound_ReturnsEmptyCart() {
        // Arrange (Setup): Prepare test data
        Long userId = 12345L;

        // Tell the mock repository: "When someone asks for cart with userId 12345, return empty (no cart found)"
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act (Execute): Call the method we want to test
        CartDto result = cartService.getCart(userId);

        // Assert (Verify): Check if the result is what we expect
        assertNotNull(result, "Result should not be null");
        assertEquals(userId, result.getUserId(), "UserId should match");
        assertNotNull(result.getItems(), "Items list should not be null");
        assertTrue(result.getItems().isEmpty(), "Items list should be empty");
        assertEquals(0, result.getTotalQuantity(), "Total quantity should be 0");

        // Verify that the repository was called once with the correct userId
        verify(cartRepository, times(1)).findByUserId(userId);
    }


    @Test
    @DisplayName("Test 3: addItemToCart should add new item successfully")
    void testAddItemToCart_NewItem_AddsSuccessfully() {
        // Arrange (Setup): Prepare test data
        Long userId = 12345L;
        String skuId = "MTA-456";
        Integer quantity = 3;

        // Create request DTO
        AddToCartRequestDto request = new AddToCartRequestDto();
        request.setSkuId(skuId);
        request.setQuantity(quantity);

        // Create product response from product service
        ProductResponseDto product = new ProductResponseDto();
        product.setSkuId(skuId);
        product.setName("New Product");
        product.setPrice(2000L);

        // Create API response wrapper
        ApiResponse<ProductResponseDto> productResponse = new ApiResponse<>();
        productResponse.setSuccess(true);
        productResponse.setData(product);
        productResponse.setStatus(200);
        productResponse.setStatusText("OK");
        productResponse.setTimestamp(java.time.Instant.now());

        // Tell the mocks what to return
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty()); // No cart exists
        when(productFeign.getProduct(skuId)).thenReturn(productResponse);
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act (Execute): Call the method we want to test
        String result = cartService.addItemToCart(userId, request);

        // Assert (Verify): Check if the result is what we expect
        assertEquals("ADDED", result, "Should return 'ADDED' for new item");

        // Verify that methods were called
        verify(cartRepository, times(1)).findByUserId(userId);
        verify(productFeign, times(1)).getProduct(skuId);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }
}