package com.gdn.training.product.model.response;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PagedResponse<T> {
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private List<T> content;
}
