package com.marketplace.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchResponse {
    private List<ProductSearchResult> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductSearchResult {
        private String productId;
        private String title;
        private String description;
        private BigDecimal price;
        private String imageUrl;
        private String category;
    }
}
