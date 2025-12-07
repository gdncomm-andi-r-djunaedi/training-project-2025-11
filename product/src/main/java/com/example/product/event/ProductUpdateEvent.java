package com.example.product.event;

import com.example.product.dto.ProductResponseDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductUpdateEvent {

    private String eventType;
    private long productId;
    private ProductResponseDTO product;
    private Long timestamp;
}
