package com.gdn.project.waroenk.catalog.dto.inventory;

import java.util.List;

/**
 * Response DTO for bulk adjust stock operation.
 */
public record BulkAdjustStockResponseDto(
    List<StockOperationResultDto> results,
    boolean allSuccess,
    int successCount,
    int failureCount
) {}



