package com.microservice.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CartItemDto {
    private Long id;
    private Long productId;

    private Integer quantity;

    private Long price;

    private Date addedAt;

    private String sku;
    private String name;
    private String description;
    private String category;
    private String brand;
    private Long itemCode;
    private Long length;
    private Long height;
    private Long width;
    private Long weight;
    private Integer dangerousLevel;
}
