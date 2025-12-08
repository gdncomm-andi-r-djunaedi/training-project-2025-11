package com.kailash.search.dto;
import lombok.*;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor

@Builder
public class ProductPayload {
    private String id;
    private String sku;
    private String name;
    private String shortDescription;
    private Double price;
    private Instant createdAt;
}
