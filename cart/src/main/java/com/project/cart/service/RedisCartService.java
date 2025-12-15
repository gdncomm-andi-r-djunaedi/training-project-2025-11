package com.project.cart.service;

import com.project.cart.entity.CartItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Service for Redis cart operations
 * Handles fast cart operations using Redis as primary storage
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCartService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${cart.ttl-days}")
    private int ttlDays;

    private static final String CART_KEY_PREFIX = "cart:";

    /**
     * Get cart key for user
     */
    private String getCartKey(String userId) {
        return CART_KEY_PREFIX + userId;
    }

    /**
     * Get cart item key
     */
    private String getItemKey(String productId) {
        return "product:" + productId;
    }

    /**
     * Add or update item in Redis cart
     */
    public void addItem(String userId, CartItem item) {
        String cartKey = getCartKey(userId);
        String itemKey = getItemKey(item.getProductId());

        HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
        hashOps.put(cartKey, itemKey, item);

        // Set expiration
        redisTemplate.expire(cartKey, ttlDays, TimeUnit.DAYS);

        log.debug("Added item to Redis cart: userId={}, productId={}", userId, item.getProductId());
    }

    /**
     * Get item from Redis cart
     */
    public CartItem getItem(String userId, String productId) {
        String cartKey = getCartKey(userId);
        String itemKey = getItemKey(productId);

        HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
        Object item = hashOps.get(cartKey, itemKey);

        return item != null ? (CartItem) item : null;
    }

    /**
     * Get all items from Redis cart
     */
    public List<CartItem> getAllItems(String userId) {
        String cartKey = getCartKey(userId);

        HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
        Map<String, Object> entries = hashOps.entries(cartKey);

        List<CartItem> items = new ArrayList<>();
        for (Object value : entries.values()) {
            items.add((CartItem) value);
        }

        log.debug("Retrieved {} items from Redis cart: userId={}", items.size(), userId);
        return items;
    }

    /**
     * Remove item from Redis cart
     */
    public void removeItem(String userId, String productId) {
        String cartKey = getCartKey(userId);
        String itemKey = getItemKey(productId);

        HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
        hashOps.delete(cartKey, itemKey);

        log.debug("Removed item from Redis cart: userId={}, productId={}", userId, productId);
    }

    /**
     * Clear entire cart
     */
    public void clearCart(String userId) {
        String cartKey = getCartKey(userId);
        redisTemplate.delete(cartKey);

        log.debug("Cleared Redis cart: userId={}", userId);
    }

    /**
     * Check if cart exists
     */
    public boolean cartExists(String userId) {
        String cartKey = getCartKey(userId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(cartKey));
    }

    /**
     * Get cart item count
     */
    public long getItemCount(String userId) {
        String cartKey = getCartKey(userId);
        HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
        return hashOps.size(cartKey);
    }

    /**
     * Update item quantity
     */
    public void updateItemQuantity(String userId, String productId, Integer quantity) {
        CartItem item = getItem(userId, productId);
        if (item != null) {
            item.setQuantity(quantity);
            item.setSubtotal(item.getPrice().multiply(new java.math.BigDecimal(quantity)));
            addItem(userId, item);

            log.debug("Updated item quantity in Redis: userId={}, productId={}, quantity={}",
                    userId, productId, quantity);
        }
    }

    /**
     * Refresh cart TTL
     */
    public void refreshTTL(String userId) {
        String cartKey = getCartKey(userId);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cartKey))) {
            redisTemplate.expire(cartKey, ttlDays, TimeUnit.DAYS);
        }
    }
}
