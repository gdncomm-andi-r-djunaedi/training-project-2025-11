package com.example.api_gateway.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddToCartResponse {
    private String productId;
    private Integer productQuantity;
    private Double totalPrice;
    private Double itemPrice;
    private String productName;
}

