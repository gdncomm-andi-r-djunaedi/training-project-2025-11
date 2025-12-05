package com.example.marketplace.product.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @Column(length=36)
    private String id;

    @Column(nullable=false)
    private String name;

    @Column(length=2000)
    private String description;

    private double price;

    public Product() {}

    public Product(String id, String name, String description, double price) {
        this.id=id; this.name=name; this.description=description; this.price=price;
    }

    public String getId(){return id;} public void setId(String id){this.id=id;}
    public String getName(){return name;} public void setName(String name){this.name=name;}
    public String getDescription(){return description;} public void setDescription(String d){this.description=d;}
    public double getPrice(){return price;} public void setPrice(double p){this.price=p;}
}
