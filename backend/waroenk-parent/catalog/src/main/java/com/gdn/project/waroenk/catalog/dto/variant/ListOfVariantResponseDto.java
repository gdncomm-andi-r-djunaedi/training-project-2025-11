package com.gdn.project.waroenk.catalog.dto.variant;

import java.util.List;

public record ListOfVariantResponseDto(
    List<VariantResponseDto> data,
    String nextToken,
    Integer total
) {}






