package com.microservice.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProductDto implements Serializable {
    private String skuId;
    private String name;
    private String description;
    private String category;
    private String brand;
    private Long price;
    private Long itemCode;
    private Long length;
    private Long height;
    private Long width;
    private Long weight;
    private Integer dangerousLevel;
}
