package org.edmund.cart.dto;

import lombok.Data;

@Data
public class AddToCartDto {
    private String productSku;
    private int quantity;
}