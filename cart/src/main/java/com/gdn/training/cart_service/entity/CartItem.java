package com.gdn.training.cart_service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItem {

    private Long productId;

    private String productName;

    private BigDecimal price;

    private Integer quantity;

    private String imageUrl;
}
