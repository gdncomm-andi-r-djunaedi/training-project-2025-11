package com.gdn.project.waroenk.catalog.dto.inventory;

import java.util.List;

public record BulkUpdateStockResponseDto(
    List<InventoryResponseDto> data,
    Integer successCount,
    Integer failureCount
) {}






