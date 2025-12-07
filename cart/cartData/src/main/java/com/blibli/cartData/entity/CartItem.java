package com.blibli.cartData.entity;


import com.blibli.cartData.dto.CartItemDTO;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@ToString
public class CartItem {

    private String productId;
    private int quantity;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }


    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }


    public CartItem(String productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

}
