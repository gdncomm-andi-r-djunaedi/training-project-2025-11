package com.gdn.project.waroenk.catalog.dto.merchant;

import java.util.List;

public record ListOfMerchantResponseDto(
    List<MerchantResponseDto> data,
    String nextToken,
    Integer total
) {}






