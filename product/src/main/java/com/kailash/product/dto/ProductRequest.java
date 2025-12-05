package com.kailash.product.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    private String sku;
    private String name;
    private String shortDescription;
    private Double price;
}