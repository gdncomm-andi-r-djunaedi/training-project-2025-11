package com.gdn.training.cart.domain.model;

import com.gdn.training.cart.domain.exception.InvalidQuantityException;

import java.util.Objects;
import java.util.UUID;

/**
 * Value / Entity representing an item inside the Cart aggregate.
 *
 * Note: treated as part of the aggregate; persistence id kept here but
 * mapping to JPA entity will live in infrastructure layer.
 */
public class CartItem {
    private final UUID id;
    private final UUID productId;
    private int quantity;

    public CartItem(UUID id, UUID productId, int quantity) {
        this.id = Objects.requireNonNull(id, "Cart item ID cannot be null");
        this.productId = Objects.requireNonNull(productId, "Product ID cannot be null");
        if (quantity <= 0) {
            throw new InvalidQuantityException("Quantity must be greater than 0");
        }
        this.quantity = quantity;
    }

    public UUID getId() {
        return id;
    }

    public UUID getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new InvalidQuantityException("Quantity must be greater than 0");
        }
        this.quantity = quantity;
    }

    public void increaseQuantity(int quantity) {
        this.quantity += quantity;
    }

    public void decreaseQuantity(int quantity) {
        this.quantity -= quantity;
    }

    @Override
    public String toString() {
        return "CartItem [id=" + id + ", productId=" + productId + ", quantity=" + quantity + "]";
    }
}
