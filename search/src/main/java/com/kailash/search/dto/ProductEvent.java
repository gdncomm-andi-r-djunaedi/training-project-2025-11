package com.kailash.search.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductEvent {
    private String eventType;
    private String productId;
    private ProductPayload payload;
    private Instant timestamp;
}
