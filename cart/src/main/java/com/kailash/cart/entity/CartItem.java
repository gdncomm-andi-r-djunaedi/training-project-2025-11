package com.kailash.cart.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private String sku;
    private String productName;
    private int qty;
    private Double priceSnapshot;
}
