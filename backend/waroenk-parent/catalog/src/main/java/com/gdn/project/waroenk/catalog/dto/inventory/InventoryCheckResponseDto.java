package com.gdn.project.waroenk.catalog.dto.inventory;

import java.util.List;

/**
 * Response DTO for batch inventory check.
 */
public record InventoryCheckResponseDto(
    List<InventoryCheckItemDto> items,
    int totalFound,
    int totalRequested,
    long took
) {}



