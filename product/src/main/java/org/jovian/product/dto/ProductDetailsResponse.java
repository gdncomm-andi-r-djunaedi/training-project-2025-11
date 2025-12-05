package org.jovian.product.dto;

public class ProductDetailsResponse {
    private String id;
    private String productName;
    private Double productPrice;
    private String productCategory;
    private String producerName;

    public ProductDetailsResponse(String id,
                                  String productName,
                                  Double productPrice,
                                  String productCategory,
                                  String producerName) {
        this.id = id;
        this.productName = productName;
        this.productPrice = productPrice;
        this.productCategory = productCategory;
        this.producerName = producerName;
    }

    public String getId() {
        return id;
    }

    public String getProductName() {
        return productName;
    }

    public Double getProductPrice() {
        return productPrice;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public String getProducerName() {
        return producerName;
    }
}
