package com.blibli.apigateway.dto.request;

import java.util.List;

public class ProductDto {
    private String productCode;
    private String name;
    private String brand;
    private String description;
    private Double price;
    private String Image;
    private List<String> category;

    public ProductDto() {
    }

    public ProductDto(String productCode, String name, String brand, String description, Double price, String Image, List<String> category) {
        this.productCode = productCode;
        this.name = name;
        this.brand = brand;
        this.description = description;
        this.price = price;
        this.Image = Image;
        this.category = category;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String Image) {
        this.Image = Image;
    }

    public List<String> getCategory() {
        return category;
    }

    public void setCategory(List<String> category) {
        this.category = category;
    }
}
