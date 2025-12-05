package com.example.product.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.TextIndexed;
import lombok.*;

@Document(collection = "products")
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    private String id;
    
    private String sku;
    
    @TextIndexed
    private String name;
    
    @TextIndexed
    private String description;
    
    private double price;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    // Builder pattern
    public static ProductBuilder builder() {
        return new ProductBuilder();
    }

    public static class ProductBuilder {
        private String id;
        private String sku;
        private String name;
        private String description;
        private double price;

        public ProductBuilder id(String id) {
            this.id = id;
            return this;
        }

        public ProductBuilder sku(String sku) {
            this.sku = sku;
            return this;
        }

        public ProductBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ProductBuilder description(String description) {
            this.description = description;
            return this;
        }

        public ProductBuilder price(double price) {
            this.price = price;
            return this;
        }

        public Product build() {
            Product product = new Product();
            product.setId(this.id);
            product.setSku(this.sku);
            product.setName(this.name);
            product.setDescription(this.description);
            product.setPrice(this.price);
            return product;
        }
    }
}
