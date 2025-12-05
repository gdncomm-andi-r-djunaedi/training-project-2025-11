
package com.example.marketplace.cart.dto;

public class CartDto {
    private String id;
    private String userId;
    private Long productId;
    private Integer quantity;

    public String getId(){return id;}
    public void setId(String id){this.id=id;}
    public String getUserId(){return userId;}
    public void setUserId(String userId){this.userId=userId;}
    public Long getProductId(){return productId;}
    public void setProductId(Long productId){this.productId=productId;}
    public Integer getQuantity(){return quantity;}
    public void setQuantity(Integer quantity){this.quantity=quantity;}
}
