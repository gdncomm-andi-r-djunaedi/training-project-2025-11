package com.elfrida.cart.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItem {

    private String productId;
    private Integer quantity;
    private BigDecimal totalPrice;
}


