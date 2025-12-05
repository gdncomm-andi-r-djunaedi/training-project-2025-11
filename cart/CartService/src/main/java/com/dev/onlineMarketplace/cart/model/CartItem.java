package com.dev.onlineMarketplace.cart.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private String productId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
}
