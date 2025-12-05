package com.gdn.project.waroenk.catalog.dto.variant;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record VariantResponseDto(
    String id,
    String sku,
    String subSku,
    String title,
    Double price,
    Boolean isDefault,
    Map<String, Object> attributes,
    String thumbnail,
    List<VariantMediaDto> media,
    Instant createdAt,
    Instant updatedAt
) {
  public record VariantMediaDto(
      String url,
      String type,
      Integer sortOrder,
      String altText
  ) {}
}
