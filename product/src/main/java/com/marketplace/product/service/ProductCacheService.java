package com.marketplace.product.service;

import com.marketplace.product.config.ProductConfigProperties;
import com.marketplace.product.document.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductConfigProperties properties;

    private static final String PRODUCT_KEY_PREFIX = "product:";

    public Optional<Product> getProduct(String productId) {
        if (!properties.getCache().isEnabled()) {
            return Optional.empty();
        }

        try {
            String key = PRODUCT_KEY_PREFIX + productId;
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof Product) {
                log.debug("Cache HIT for product: {}", productId);
                return Optional.of((Product) cached);
            }
        } catch (Exception e) {
            log.error("Error retrieving product from cache: {}", e.getMessage());
        }

        log.debug("Cache MISS for product: {}", productId);
        return Optional.empty();
    }

    public void cacheProduct(Product product) {
        if (!properties.getCache().isEnabled() || product == null || product.getId() == null) {
            return;
        }

        try {
            String key = PRODUCT_KEY_PREFIX + product.getId();
            long ttl = properties.getCache().getTtlMinutes();
            redisTemplate.opsForValue().set(key, product, Duration.ofMinutes(ttl));
            log.debug("Cached product: {} with TTL: {} minutes", product.getId(), ttl);
        } catch (Exception e) {
            log.error("Error caching product: {}", e.getMessage());
        }
    }
}

