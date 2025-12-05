package com.gdn.training.cart.model.response;

import java.util.List;

import com.gdn.training.cart.model.entity.CartItem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private String message;
    private List<CartItem> cartItems;
    private double total;
    private int itemCount;
    private String userEmail;
}
