package com.gdn.project.waroenk.catalog.dto.inventory;

import java.util.List;

/**
 * Response DTO for bulk stock operations (lock, acquire, release).
 */
public record BulkStockOperationResponseDto(
    String checkoutId,
    List<StockOperationResultDto> results,
    boolean allSuccess,
    int successCount,
    int failureCount
) {}



