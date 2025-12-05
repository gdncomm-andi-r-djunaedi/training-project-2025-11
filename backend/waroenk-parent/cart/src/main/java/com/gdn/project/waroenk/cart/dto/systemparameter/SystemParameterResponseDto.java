package com.gdn.project.waroenk.cart.dto.systemparameter;

import java.time.Instant;

public record SystemParameterResponseDto(
    String id,
    String variable,
    String data,
    String description,
    String type,
    Instant createdAt,
    Instant updatedAt
) {}




