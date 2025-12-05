package com.blibli.cartmodule.dto;

import java.util.List;

public class ViewCartResponseDto {

    private String message;
    private String status;
    private String action;
    private Integer totalCartQuantity;
    private Double totalPrice;
    private List<CartItemDto> items;

    public ViewCartResponseDto() {
    }

    public ViewCartResponseDto(String message, String status, String action, Integer totalCartQuantity, Double totalPrice, List<CartItemDto> items) {
        this.message = message;
        this.status = status;
        this.action = action;
        this.totalCartQuantity = totalCartQuantity;
        this.totalPrice = totalPrice;
        this.items = items;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Integer getTotalCartQuantity() {
        return totalCartQuantity;
    }

    public void setTotalCartQuantity(Integer totalCartQuantity) {
        this.totalCartQuantity = totalCartQuantity;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<CartItemDto> getItems() {
        return items;
    }

    public void setItems(List<CartItemDto> items) {
        this.items = items;
    }
}


