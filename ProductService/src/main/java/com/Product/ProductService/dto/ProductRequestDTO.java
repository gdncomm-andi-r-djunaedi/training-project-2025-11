package com.Product.ProductService.dto;

import lombok.Data;

@Data
public class ProductRequestDTO {
    private String productName;
    private String productDescription;
    private double price;
    private String category;
}
