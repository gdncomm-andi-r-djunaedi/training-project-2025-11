package com.training.productService.productmongo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO implements Serializable {
    private String id;
    private String sku;
    private String name;
    private String description;
    private Double price;
    private String category;
    private List<String> tags;
    private List<String> images;
}
