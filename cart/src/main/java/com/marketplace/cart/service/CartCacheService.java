package com.marketplace.cart.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.cart.cache.CartCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartCacheService {

    private static final String CART_CACHE_PREFIX = "cart:";
    private static final long CART_CACHE_TTL_HOURS = 24;

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public Optional<CartCache> getCart(UUID memberId) {
        String key = getKey(memberId);
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                if (cached instanceof CartCache) {
                    return Optional.of((CartCache) cached);
                }
                // Handle JSON string from Redis
                CartCache cart = objectMapper.convertValue(cached, CartCache.class);
                return Optional.of(cart);
            }
        } catch (Exception e) {
            log.error("Error getting cart from cache for member: {}", memberId, e);
        }
        return Optional.empty();
    }

    public void saveCart(CartCache cart) {
        String key = getKey(cart.getMemberId());
        cart.setLastModified(LocalDateTime.now());
        try {
            redisTemplate.opsForValue().set(key, cart, CART_CACHE_TTL_HOURS, TimeUnit.HOURS);
            log.debug("Cart saved to cache for member: {}", cart.getMemberId());
        } catch (Exception e) {
            log.error("Error saving cart to cache for member: {}", cart.getMemberId(), e);
        }
    }

    public void deleteCart(UUID memberId) {
        String key = getKey(memberId);
        try {
            redisTemplate.delete(key);
            log.debug("Cart deleted from cache for member: {}", memberId);
        } catch (Exception e) {
            log.error("Error deleting cart from cache for member: {}", memberId, e);
        }
    }

    public void markDirty(UUID memberId) {
        getCart(memberId).ifPresent(cart -> {
            cart.setDirty(true);
            saveCart(cart);
        });
    }

    public Set<String> getAllDirtyCartKeys() {
        try {
            return redisTemplate.keys(CART_CACHE_PREFIX + "*");
        } catch (Exception e) {
            log.error("Error getting dirty cart keys", e);
            return Set.of();
        }
    }

    private String getKey(UUID memberId) {
        return CART_CACHE_PREFIX + memberId;
    }
}

