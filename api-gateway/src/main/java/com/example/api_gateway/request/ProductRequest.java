package com.example.api_gateway.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    private String itemSku;
    private String productName;
    private Double productPrice;
    private String productDescription;
}

