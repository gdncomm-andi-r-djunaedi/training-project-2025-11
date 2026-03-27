package com.blibli.CartService.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItem {

    private String productId;
    private String productName;
    private BigDecimal price;
    private String description;
    private int quantity;
}
