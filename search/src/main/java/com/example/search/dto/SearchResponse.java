package com.example.search.dto;

import com.example.search.utils.PaginationMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {
    private List<ProductDocumentDto> products;
    private PaginationMetadata metadata;
}
