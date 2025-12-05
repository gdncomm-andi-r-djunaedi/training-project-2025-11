package com.gdn.marketplace.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddToCartRequest {
    private String productId;
    private String productName;
    private Double price;
    private Integer quantity;

}
