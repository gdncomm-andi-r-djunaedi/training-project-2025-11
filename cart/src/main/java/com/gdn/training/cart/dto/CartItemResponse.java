package com.gdn.training.cart.dto;

import lombok.Data;

@Data
public class CartItemResponse {
    private String id;
    private String productId;
    private String productName;
    private Double price;
    private Integer quantity;
}
