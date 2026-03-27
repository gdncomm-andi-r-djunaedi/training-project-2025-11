package com.example.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddToCartRequest {

    private UUID customerId;
    private String customerName;
    private String productId;
    private Integer productQuantity;
    private Double itemPrice;
    private String productName;
}
