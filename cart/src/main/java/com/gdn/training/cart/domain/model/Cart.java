package com.gdn.training.cart.domain.model;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.gdn.training.cart.domain.exception.InvalidQuantityException;
import com.gdn.training.cart.domain.exception.ItemNotFoundException;

/**
 * Aggregate root: Cart
 *
 * Pure domain object: contains business behaviors for managing cart items.
 * No framework or persistence annotations here.
 */
public class Cart {
    private final UUID id;
    private final UUID memberId;
    private final Instant createdAt;
    private Instant updatedAt;
    private final List<CartItem> items;

    /**
     * Constructor for Cart.
     *
     * @param id        The unique identifier of the cart.
     * @param memberId  The unique identifier of the member associated with the
     *                  cart.
     * @param createdAt The timestamp when the cart was created.
     * @param updatedAt The timestamp when the cart was last updated.
     * @param items     The list of cart items.
     */
    public Cart(UUID id, UUID memberId, Instant createdAt, Instant updatedAt, List<CartItem> items) {
        this.id = Objects.requireNonNull(id, "Cart ID cannot be null");
        this.memberId = Objects.requireNonNull(memberId, "Member ID cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "Created at cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "Updated at cannot be null");
        this.items = new ArrayList<>();
        if (items != null) {
            for (CartItem cartItem : items) {
                this.items.add(cartItem);
            }
        }
    }

    /*
     * factory method
     */
    public static Cart createCart(UUID memberId) {
        return new Cart(UUID.randomUUID(), memberId, Instant.now(), Instant.now(), null);
    }

    // getters
    public UUID getId() {
        return id;
    }

    public UUID getMemberId() {
        return memberId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /*
     * business behaviors
     */
    public void addItem(UUID productId, int quantity) {
        if (quantity <= 0) {
            throw new InvalidQuantityException("Quantity must be greater than 0");
        }
        Optional<CartItem> existingItem = findItemByProductId(productId);
        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + quantity);
        } else {
            items.add(new CartItem(UUID.randomUUID(), productId, quantity));
        }

        touchUpdatedAt();
    }

    /*
     * update item
     */
    public void updateItem(UUID productId, int quantity) {
        if (quantity <= 0) {
            throw new InvalidQuantityException("Quantity must be greater than 0");
        }
        Optional<CartItem> existingItem = findItemByProductId(productId);
        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(quantity);
        } else {
            throw new ItemNotFoundException("Item not found");
        }

        touchUpdatedAt();
    }

    /*
     * remove item
     */
    public void removeItem(UUID productId) {
        Optional<CartItem> existingItem = findItemByProductId(productId);
        if (existingItem.isPresent()) {
            items.remove(existingItem.get());
        } else {
            throw new ItemNotFoundException("Item not found");
        }

        touchUpdatedAt();
    }

    /*
     * clear cart
     */
    public void clearCart() {
        items.clear();
        touchUpdatedAt();
    }

    public Optional<CartItem> findItemByProductId(UUID productId) {
        return items.stream().filter(item -> item.getProductId().equals(productId)).findFirst();
    }

    /*
     * touch updated at
     */
    public void touchUpdatedAt() {
        this.updatedAt = Instant.now();
    }

    @Override
    public String toString() {
        return "Cart [id=" + id + ", memberId=" + memberId + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt
                + ", items=" + items + "]";
    }
}
