package com.example.marketplace.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class AddToCartRequestDTO {
    @NotBlank
    private String productId;
    @Min(1)
    private int quantity = 1;

    public String getProductId(){return productId;} public void setProductId(String p){this.productId=p;}
    public int getQuantity(){return quantity;} public void setQuantity(int q){this.quantity=q;}
}
