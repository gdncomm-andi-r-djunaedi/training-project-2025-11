package com.Cart.CartService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponseDTO {
    private String itemId;
    private String productId;

    private String productName;
    private Double price;
    private String productDescription;
    private String category;

    private int quantity;
}
