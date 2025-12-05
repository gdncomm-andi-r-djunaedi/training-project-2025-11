package com.elfrida.product.dto;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {
    private String id;
    private String name;
    private Integer price;
    private Integer stock;
}
