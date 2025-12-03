package com.microservice.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductSearchDto {
    private String searchTerm;
    private String category;
    private String brand;
    private Long minPrice;
    private Long maxPrice;
    private Boolean isActive;
    private Integer dangerousLevel;
    private Integer storeId;
    private int page = 0;
    private int size = 10;
}

