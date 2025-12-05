package com.gdn.project.waroenk.cart.dto;

import java.time.LocalDateTime;

public record ErrorResponseDto(
    Integer code,
    String status,
    String message,
    LocalDateTime timestamp
) {}




