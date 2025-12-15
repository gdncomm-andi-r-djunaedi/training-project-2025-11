package com.example.cart.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.*;

import java.io.Serializable;

@Document(collection = "cart_items")
@NoArgsConstructor
@AllArgsConstructor
public class CartItem implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    private String id;
    
    @Indexed
    private String username;
    
    private String sku;
    private String name;
    private int qty;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    // Builder pattern
    public static CartItemBuilder builder() {
        return new CartItemBuilder();
    }

    public static class CartItemBuilder {
        private String id;
        private String username;
        private String sku;
        private String name;
        private int qty;

        public CartItemBuilder id(String id) {
            this.id = id;
            return this;
        }

        public CartItemBuilder username(String username) {
            this.username = username;
            return this;
        }

        public CartItemBuilder sku(String sku) {
            this.sku = sku;
            return this;
        }

        public CartItemBuilder name(String name) {
            this.name = name;
            return this;
        }

        public CartItemBuilder qty(int qty) {
            this.qty = qty;
            return this;
        }

        public CartItem build() {
            CartItem cartItem = new CartItem();
            cartItem.setId(this.id);
            cartItem.setUsername(this.username);
            cartItem.setSku(this.sku);
            cartItem.setName(this.name);
            cartItem.setQty(this.qty);
            return cartItem;
        }
    }
}
