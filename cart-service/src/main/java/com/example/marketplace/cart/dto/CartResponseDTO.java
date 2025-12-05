package com.example.marketplace.cart.dto;

import java.util.List;

public class CartResponseDTO {
    private String userId;
    private List<CartItemDTO> items;

    public String getUserId(){return userId;} public void setUserId(String u){this.userId=u;}
    public List<CartItemDTO> getItems(){return items;} public void setItems(List<CartItemDTO> items){this.items=items;}
}
