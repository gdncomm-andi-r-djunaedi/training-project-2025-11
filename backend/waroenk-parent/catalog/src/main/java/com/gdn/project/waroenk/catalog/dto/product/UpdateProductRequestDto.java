package com.gdn.project.waroenk.catalog.dto.product;

import java.util.List;

public record UpdateProductRequestDto(
    String title,
    String sku,
    String merchantCode,
    String categoryId,
    String brandId,
    ProductSummaryDto summary,
    String detailRef
) {
  public record ProductSummaryDto(
      String shortDescription,
      List<String> tags
  ) {}
}
