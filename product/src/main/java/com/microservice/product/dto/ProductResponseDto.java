package com.microservice.product.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProductResponseDto implements Serializable {
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
}