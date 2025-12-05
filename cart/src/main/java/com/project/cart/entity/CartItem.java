package com.project.cart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Cart item representing a product in the cart
 * Embedded in Cart document
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    private String productId;

    private String productSku;

    private String productName;

    private String productImage;

    private Integer quantity;

    private BigDecimal price;

    private BigDecimal subtotal;

    private LocalDateTime addedAt;
}
