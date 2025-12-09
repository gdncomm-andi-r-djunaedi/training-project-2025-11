package com.marketplace.cart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem implements Serializable {
    
    private String productId;
    private String productName;
    private Double price;
    private Integer quantity;
    
    public Double getSubtotal() {
        return price * quantity;
    }
}
