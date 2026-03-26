package com.Cart.CartService.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public class ErrorResponseDTO {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}
