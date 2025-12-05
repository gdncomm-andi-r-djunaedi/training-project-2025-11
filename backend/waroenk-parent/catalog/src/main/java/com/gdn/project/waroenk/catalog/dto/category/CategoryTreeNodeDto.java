package com.gdn.project.waroenk.catalog.dto.category;

import java.util.List;

public record CategoryTreeNodeDto(
    String id,
    String name,
    String iconUrl,
    String slug,
    List<CategoryTreeNodeDto> children
) {}






