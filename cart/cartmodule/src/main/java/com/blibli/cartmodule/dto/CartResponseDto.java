package com.blibli.cartmodule.dto;

public class CartResponseDto {

    private String message;
    private String status;
    private String action;
    private String productCode;
    private Integer quantity;

    public CartResponseDto() {
    }

    public CartResponseDto(String message, String status, String action, String productCode, Integer quantity) {
        this.message = message;
        this.status = status;
        this.action = action;
        this.productCode = productCode;
        this.quantity = quantity;
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

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}

