package com.gdn.project.waroenk.catalog.dto.inventory;

import java.util.List;

public record ListOfInventoryResponseDto(
    List<InventoryResponseDto> data,
    String nextToken,
    Integer total
) {}






