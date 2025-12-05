package com.microservice.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for Kafka events representing product lifecycle events
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductEventDto implements Serializable {

    private static final long serialVersionUID = 1L;
    private EventType eventType;
    private String skuId;
    private Integer storeId;
    private String name;
    private String description;
    private String category;
    private String brand;
    private Long price;
    private Long itemCode;
    private Boolean isActive;
    private Long length;
    private Long height;
    private Long width;
    private Long weight;
    private Integer dangerousLevel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime eventTimestamp;
    public enum EventType {
        CREATED,
        UPDATED,
        DELETED
    }
}





