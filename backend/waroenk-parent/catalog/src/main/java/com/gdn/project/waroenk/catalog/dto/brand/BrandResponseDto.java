package com.gdn.project.waroenk.catalog.dto.brand;

import java.time.Instant;

public record BrandResponseDto(
    String id,
    String name,
    String slug,
    String iconUrl,
    Instant createdAt,
    Instant updatedAt
) {}






