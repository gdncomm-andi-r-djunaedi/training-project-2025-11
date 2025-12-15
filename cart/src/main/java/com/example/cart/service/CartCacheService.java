package com.example.cart.service;

import com.example.cart.entity.CartItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CartCacheService {
    private static final Logger log = LoggerFactory.getLogger(CartCacheService.class);
    private static final String CART_KEY_PREFIX = "cart:";
    private static final long CACHE_TTL_HOURS = 24;
    
    private final RedisTemplate<String, Object> redisTemplate;

    public CartCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void cacheCart(String username, List<CartItem> items) {
        try {
            String key = CART_KEY_PREFIX + username;
            redisTemplate.opsForValue().set(key, items, CACHE_TTL_HOURS, TimeUnit.HOURS);
            log.info("Cached cart for user: {} with {} items", username, items.size());
        } catch (Exception e) {
            log.error("Failed to cache cart for user: {} - {}", username, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public List<CartItem> getCachedCart(String username) {
        try {
            String key = CART_KEY_PREFIX + username;
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof List) {
                log.info("Cache HIT for user: {}", username);
                return (List<CartItem>) cached;
            }
            log.info("Cache MISS for user: {}", username);
            return null;
        } catch (Exception e) {
            log.error("Failed to get cached cart for user: {} - {}", username, e.getMessage());
            return null;
        }
    }

    public void invalidateCache(String username) {
        try {
            String key = CART_KEY_PREFIX + username;
            Boolean deleted = redisTemplate.delete(key);
            log.info("Invalidated cache for user: {}, deleted: {}", username, deleted);
        } catch (Exception e) {
            log.error("Failed to invalidate cache for user: {} - {}", username, e.getMessage());
        }
    }
}
