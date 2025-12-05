package com.gdn.project.waroenk.catalog.dto.brand;

import java.util.List;

public record ListOfBrandResponseDto(
    List<BrandResponseDto> data,
    String nextToken,
    Integer total
) {}






