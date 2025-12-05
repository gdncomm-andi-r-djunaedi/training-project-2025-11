package com.blibli.ProductService.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto {

    private String sku;
    private String productName;
    private String description;
    private BigDecimal price;
    private String category;
}
