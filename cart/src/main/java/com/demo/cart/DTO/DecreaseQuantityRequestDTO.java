package com.demo.cart.DTO;

import lombok.Data;

@Data
public class DecreaseQuantityRequestDTO {
    private String cartItemId;
    private Integer quantity;
}

