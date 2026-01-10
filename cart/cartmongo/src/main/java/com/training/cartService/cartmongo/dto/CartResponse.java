package com.training.cartService.cartmongo.dto;

import com.training.cartService.cartmongo.entity.CartItemEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private double totalPrice;
    private int totalQuantity;
    private List<CartItemEntity> items;
}
