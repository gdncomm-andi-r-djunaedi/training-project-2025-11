package com.training.marketplace.gateway.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCartDTO {
    private String productId;
    private String productName;
    private double productPrice;
    private String productImage;
    private int productCartQuantity;
}
