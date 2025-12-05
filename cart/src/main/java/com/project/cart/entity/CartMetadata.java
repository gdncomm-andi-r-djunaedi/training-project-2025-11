package com.project.cart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Additional metadata for cart tracking
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartMetadata {

    private String ipAddress;

    private String userAgent;

    private LocalDateTime lastActivity;
}
