package com.example.marketplace.cart.dto;

public class CartItemDTO {
    private String itemId;
    private String productId;
    private String productName;
    private double price;
    private int quantity;

    public String getItemId(){return itemId;} public void setItemId(String id){this.itemId=id;}
    public String getProductId(){return productId;} public void setProductId(String p){this.productId=p;}
    public String getProductName(){return productName;} public void setProductName(String n){this.productName=n;}
    public double getPrice(){return price;} public void setPrice(double p){this.price=p;}
    public int getQuantity(){return quantity;} public void setQuantity(int q){this.quantity=q;}
}
