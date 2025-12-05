package com.training.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponse {
    private UUID cartId;
    private String customerEmail;
    private List<CartItemResponse> items;
    private BigDecimal totalAmount;
    private Integer totalItems;
}
