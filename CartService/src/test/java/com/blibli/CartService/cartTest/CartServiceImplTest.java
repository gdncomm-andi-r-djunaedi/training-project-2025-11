package com.blibli.CartService.cartTest;

import com.blibli.CartService.client.ProductFeignClient;
import com.blibli.CartService.dto.*;
import com.blibli.CartService.entity.CartEntity;
import com.blibli.CartService.entity.CartItem;
import com.blibli.CartService.exception.CartNotFoundException;
import com.blibli.CartService.repository.CartRepository;
import com.blibli.CartService.service.impl.CartServiceImpl;
import com.blibli.CartService.util.ApiResponse;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductFeignClient productClient;

    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    void addOrUpdateCart_addNewItem() {
        AddToCartRequest request = new AddToCartRequest("SKU-1", 2);

        ProductResponseDto product = ProductResponseDto.builder()
                .sku("SKU-1")
                .productName("Laptop")
                .description("Gaming Laptop")
                .price(BigDecimal.valueOf(50000))
                .build();

        ApiResponse<ProductResponseDto> apiResponse =
                ApiResponse.success("ok", product);

        when(cartRepository.findByUserId("1"))
                .thenReturn(Optional.empty());

        when(productClient.getProduct("SKU-1"))
                .thenReturn(apiResponse);

        when(cartRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        CartResponseDto response = cartService.addOrUpdateCart("1", request);

        assertEquals(1, response.getItems().size());
        assertEquals("SKU-1", response.getItems().get(0).getProductId());
    }

    @Test
    void viewCart_updatesPriceIfChanged() {
        CartItem item = CartItem.builder()
                .productId("SKU-1")
                .productName("Old Name")
                .description("Old Desc")
                .quantity(2)
                .price(BigDecimal.valueOf(100))
                .build();

        CartEntity cart = CartEntity.builder()
                .userId("1")
                .items(new ArrayList<>(List.of(item)))
                .build();

        ProductResponseDto product = ProductResponseDto.builder()
                .sku("SKU-1")
                .productName("New Name")
                .description("New Desc")
                .price(BigDecimal.valueOf(200))
                .build();

        ApiResponse<ProductResponseDto> apiResponse =
                ApiResponse.success("ok", product);

        when(cartRepository.findByUserId("1"))
                .thenReturn(Optional.of(cart));

        when(productClient.getProduct("SKU-1"))
                .thenReturn(apiResponse);

        CartResponseDto response = cartService.viewCart("1");

        assertEquals("New Name", response.getItems().get(0).getProductName());
        assertTrue(
                BigDecimal.valueOf(400)
                        .compareTo(response.getItems().get(0).getPrice()) == 0
        );

    }

    @Test
    void viewCart_cartNotFound_shouldThrowException() {
        when(cartRepository.findByUserId("1"))
                .thenReturn(Optional.empty());

        assertThrows(
                CartNotFoundException.class,
                () -> cartService.viewCart("1")
        );
    }

    @Test
    void deleteItem_success() {
        CartEntity cart = CartEntity.builder()
                .userId("1")
                .items(new ArrayList<>())
                .build();

        when(cartRepository.findByUserId("1"))
                .thenReturn(Optional.of(cart));

        cartService.deleteItem("1", "SKU-1");

        assertTrue(cart.getItems().isEmpty());
    }
}
