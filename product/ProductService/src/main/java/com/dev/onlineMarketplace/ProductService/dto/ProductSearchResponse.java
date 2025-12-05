package com.dev.onlineMarketplace.ProductService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchResponse {
    private int page;
    private int limit;
    private long total;
    private List<ProductDTO> items;
}
