package com.microservice.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProductResponseDto {
    private Long id;
    private Integer storeId;
    private String sku;
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
}
