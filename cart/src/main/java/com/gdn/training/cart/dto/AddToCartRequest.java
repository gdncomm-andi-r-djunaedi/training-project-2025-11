package com.gdn.training.cart.dto;

import lombok.Data;

@Data
public class AddToCartRequest {
    private Long memberId;
    private String productId;
    private int quantity;
}
