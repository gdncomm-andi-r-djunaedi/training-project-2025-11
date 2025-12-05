package com.blibli.ProductService.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEvent {

    private String eventType;
    private String sku;
    private String productName;
    private String description;
    private BigDecimal price;
    private String category;
    private Instant eventTime;
}