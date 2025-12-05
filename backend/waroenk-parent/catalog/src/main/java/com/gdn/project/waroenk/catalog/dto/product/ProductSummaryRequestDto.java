package com.gdn.project.waroenk.catalog.dto.product;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Request DTO for batch product summary lookup by subSkus.
 */
public record ProductSummaryRequestDto(
    @NotEmpty(message = "subSkus list cannot be empty")
    List<String> subSkus
) {}



