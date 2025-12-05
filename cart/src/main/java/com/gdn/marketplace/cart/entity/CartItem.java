package com.gdn.marketplace.cart.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {
    private String productId;
    private String productName;
    private int quantity;
    private BigDecimal price;
}
