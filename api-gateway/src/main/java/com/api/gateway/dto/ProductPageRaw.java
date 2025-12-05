package com.api.gateway.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProductPageRaw {
    private List<Object> content;

    private int number;
    private int size;
    private long totalElements;
    private int totalPages;
}
