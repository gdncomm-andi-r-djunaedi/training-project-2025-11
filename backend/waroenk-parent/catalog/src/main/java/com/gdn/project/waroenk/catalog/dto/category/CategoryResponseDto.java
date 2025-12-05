package com.gdn.project.waroenk.catalog.dto.category;

import java.time.Instant;

public record CategoryResponseDto(
    String id,
    String name,
    String slug,
    String iconUrl,
    String parentId,
    Instant createdAt,
    Instant updatedAt
) {}






