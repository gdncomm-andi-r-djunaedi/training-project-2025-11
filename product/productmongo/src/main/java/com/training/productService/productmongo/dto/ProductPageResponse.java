package com.training.productService.productmongo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductPageResponse implements Serializable {
    private List<ProductDTO> content;
    private PageableInfo pageable;
    private long totalElements;
    private int totalPages;
    private boolean last;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageableInfo implements Serializable {
        private int pageNumber;
        private int pageSize;
    }
}
