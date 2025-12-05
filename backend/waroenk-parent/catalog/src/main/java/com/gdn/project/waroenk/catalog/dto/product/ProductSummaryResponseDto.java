package com.gdn.project.waroenk.catalog.dto.product;

import java.util.List;

/**
 * Response DTO for batch product summary lookup.
 */
public record ProductSummaryResponseDto(
    List<AggregatedProductDto> products,
    int totalFound,
    int totalRequested,
    long took
) {}



