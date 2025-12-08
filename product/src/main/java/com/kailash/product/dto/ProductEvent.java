package com.kailash.product.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class ProductEvent {
    private String eventType;
    private String productId;
    private ProductPayload payload;
    private Instant timestamp;
}
