package com.gdn.project.waroenk.catalog.dto.product;

import java.util.List;

public record ListOfProductResponseDto(
    List<ProductResponseDto> data,
    String nextToken,
    Integer total
) {}






