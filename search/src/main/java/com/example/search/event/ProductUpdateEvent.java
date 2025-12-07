package com.example.search.event;

import com.example.search.dto.ProductResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateEvent {

    private String eventType;
    private long productId;
    private ProductResponseDTO product;
    private Long timestamp;
}
