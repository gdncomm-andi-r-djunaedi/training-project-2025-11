package com.marketplace.product.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUpdateEvent {
    private String eventType;
    private String productId;
    private String title;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private String category;
}
