package com.kailash.product.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class ProductPayload {
    private String id;
    private String sku;
    private String name;
    private String shortDescription;
    private Double price;
    private Instant createdAt;
}
