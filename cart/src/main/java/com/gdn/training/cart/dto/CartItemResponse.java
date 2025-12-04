package com.gdn.training.cart.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemResponse {
    private String id;
    private String productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
}
