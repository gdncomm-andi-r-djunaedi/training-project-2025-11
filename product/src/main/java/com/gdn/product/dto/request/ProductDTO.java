package com.gdn.product.dto.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductDTO {
    private String productId;
    private String productName;
    private double price ;
    private String description;
    private String brand;
    private String category;
}
