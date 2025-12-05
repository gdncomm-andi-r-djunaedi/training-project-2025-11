package com.microservice.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponseDto {

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
    private LocalDate createdAt;
    private LocalDate updatedAt;
}
