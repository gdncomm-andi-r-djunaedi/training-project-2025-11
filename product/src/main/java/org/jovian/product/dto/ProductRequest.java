package org.jovian.product.dto;

import jakarta.validation.constraints.NotBlank;

public class ProductRequest {

    @NotBlank(message = "Please enter a valid name")
    private String productName;

    @NotBlank(message = "Please enter a valid price")
    private Double productPrice;

    @NotBlank(message = "Please enter a valid producer code ('ABC-1234')")
    private String producerCode;

    @NotBlank(message = "Please enter a valid producer name")
    private String producerName;

    @NotBlank(message = "Please enter a valid category")
    private String productCategory;

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(Double productPrice) {
        this.productPrice = productPrice;
    }

    public String getProducerCode() {
        return producerCode;
    }

    public void setProducerCode(String producerCode) {
        this.producerCode = producerCode;
    }

    public String getProducerName() {
        return producerName;
    }

    public void setProducerName(String producerName) {
        this.producerName = producerName;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }
}
