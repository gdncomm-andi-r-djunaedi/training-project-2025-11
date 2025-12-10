package com.marketplace.cart.mapper;

import com.marketplace.cart.dto.response.CartItemResponse;
import com.marketplace.cart.dto.response.CartResponse;
import com.marketplace.cart.entity.Cart;
import com.marketplace.cart.entity.CartItem;

import java.util.stream.Collectors;

/**
 * Mapper class to convert entities to DTOs
 */
public class CartMapper {

    private CartMapper() {
        // Prevent instantiation
    }

    /**
     * Convert Cart entity to CartResponse DTO
     */
    public static CartResponse toCartResponse(Cart cart) {
        CartResponse response = CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .items(cart.getItems().stream()
                        .map(CartMapper::toCartItemResponse)
                        .collect(Collectors.toList()))
                .build();

        // Calculate totals
        response.setTotalAmount(response.calculateTotalAmount());
        response.setTotalItems(response.calculateTotalItems());

        return response;
    }

    /**
     * Convert CartItem entity to CartItemResponse DTO
     */
    public static CartItemResponse toCartItemResponse(CartItem item) {
        CartItemResponse response = CartItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .build();

        // Calculate subtotal
        response.setSubtotal(response.calculateSubtotal());

        return response;
    }
}
