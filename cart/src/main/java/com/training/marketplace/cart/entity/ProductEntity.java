package com.training.marketplace.cart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEntity {
    private String productId;
    private String productName;
    private double productPrice;
    private String productImage;
    private int productCartQuantity;
}
