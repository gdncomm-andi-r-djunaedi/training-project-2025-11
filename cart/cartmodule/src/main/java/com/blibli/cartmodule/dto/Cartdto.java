package com.blibli.cartmodule.dto;

public class Cartdto {
    private String productId;
    private int quantity;

    public Cartdto() {
    }

    public Cartdto(String productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

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
}
