package com.blibli.ProductService.dto;

import com.blibli.ProductService.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchResponseDto {

    private List<Product> products;
    private Long totalResults;
    private Integer currentPage;
    private Integer pageSize;
    private Integer totalPages;
}
