package com.example.cart.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemDTO {
    private Long Id;
    private Long productId;
    private Integer quantity;
    private BigDecimal price;
}
