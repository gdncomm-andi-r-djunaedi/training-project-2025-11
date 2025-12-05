package com.gdn.project.waroenk.catalog.dto.category;

import java.util.List;

public record ListOfCategoryResponseDto(
    List<CategoryResponseDto> data,
    String nextToken,
    Integer total
) {}






