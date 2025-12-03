package com.gdn.training.cart.model.entity;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
