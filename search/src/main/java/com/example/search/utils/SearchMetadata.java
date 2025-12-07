package com.example.search.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchMetadata {
    private int page;
    private int size;
    private long totalItems;
    private int totalPage;
    private long totalElements;
    
    public static SearchMetadata of(int page, int size, long totalItems, int totalPage, long totalElements) {
        return new SearchMetadata(page, size, totalItems, totalPage, totalElements);
    }
}
