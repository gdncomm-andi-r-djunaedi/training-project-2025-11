package com.gdn.training.product.domain.model;

import java.time.Instant;
import java.util.UUID;

import com.google.common.base.Objects;

public class Product {
    // Attributes
    private final UUID productId;
    private final UUID sellerId;
    private final String name;
    private final String description;
    private final String category;
    private final double price;
    private final int stock;
    private final Status status;
    private final Instant createdAt;

    // Enum
    public enum Status {
        ACTIVE,
        INACTIVE,
        DELETED,
        NOT_FOUND
    }

    // Constructor
    public Product(UUID productId, UUID sellerId, String name, String description, String category, double price,
            int stock, Status status, Instant createdAt) {
        if (productId == null)
            throw new IllegalArgumentException("productId required");
        if (sellerId == null)
            throw new IllegalArgumentException("sellerId required");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("name required");
        if (price < 0)
            throw new IllegalArgumentException("price must >= 0");
        if (stock < 0)
            throw new IllegalArgumentException("stock must >= 0");

        this.productId = productId;
        this.sellerId = sellerId;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters
    public UUID getProductId() {
        return productId;
    }

    public UUID getSellerId() {
        return sellerId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public Status getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Product))
            return false;
        Product product = (Product) o;
        return productId.equals(product.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(productId);
    }
}
