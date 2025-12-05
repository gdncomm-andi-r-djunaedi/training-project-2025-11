package com.microservice.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CartDto {
    private Long userId;

    private List<CartItemDto> items;

    private Integer totalQuantity;

    private Long totalPrice;

    private Date updatedAt;
}
