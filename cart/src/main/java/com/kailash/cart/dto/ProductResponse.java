package com.kailash.cart.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProductResponse {
    private String sku;
    private String name;
    private String shortDescription;
    private Double price;
}