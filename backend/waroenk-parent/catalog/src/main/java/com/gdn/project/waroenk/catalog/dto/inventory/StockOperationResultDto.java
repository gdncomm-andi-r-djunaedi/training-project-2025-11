package com.gdn.project.waroenk.catalog.dto.inventory;

/**
 * Single stock operation result.
 */
public record StockOperationResultDto(
    String subSku,
    boolean success,
    String message,
    Long currentStock,
    Long requestedQuantity
) {}



