package com.example.api_gateway.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private String itemSku;
    private String productName;
    private Double productPrice;
    private String productDescription;
}

