package com.example.search.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginationMetadata {
    private int page;
    private int size;
    private long totalItems;
    private int totalPage;
    
    public static PaginationMetadata of(int page, int size, long totalItems, int totalPage) {
        return new PaginationMetadata(page, size, totalItems, totalPage);
    }
}
