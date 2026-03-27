package com.blibli.cart.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponseDTO {
    private String productId;
    private String sku;
    private String name;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
    private Date addedAt;
}
