package com.example.marketplace.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class ProductRequestDTO {
    @NotBlank
    private String id;
    @NotBlank
    private String name;
    private String description;
    @Positive
    private double price;

    public String getId(){return id;} public void setId(String id){this.id=id;}
    public String getName(){return name;} public void setName(String name){this.name=name;}
    public String getDescription(){return description;} public void setDescription(String d){this.description=d;}
    public double getPrice(){return price;} public void setPrice(double p){this.price=p;}
}
