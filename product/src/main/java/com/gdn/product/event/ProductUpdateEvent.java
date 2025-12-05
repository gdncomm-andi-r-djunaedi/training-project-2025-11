package com.gdn.product.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateEvent {
    private String productId;
    private String productName;
    private Double price;
    private String category;
    private String brand;
}