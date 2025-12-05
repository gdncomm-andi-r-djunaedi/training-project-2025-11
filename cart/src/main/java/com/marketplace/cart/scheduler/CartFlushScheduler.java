package com.marketplace.cart.scheduler;

import com.marketplace.cart.cache.CartCache;
import com.marketplace.cart.service.CartCacheService;
import com.marketplace.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CartFlushScheduler {

    private final CartCacheService cartCacheService;
    private final CartService cartService;

    /**
     * Flush dirty carts to database every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void flushDirtyCarts() {
        log.info("Starting scheduled cart flush");
        
        Set<String> cartKeys = cartCacheService.getAllDirtyCartKeys();
        int flushedCount = 0;
        
        for (String key : cartKeys) {
            try {
                // Extract member ID from key (format: cart:uuid)
                String memberIdStr = key.replace("cart:", "");
                UUID memberId = UUID.fromString(memberIdStr);
                
                cartCacheService.getCart(memberId).ifPresent(cache -> {
                    if (cache.isDirty() && !cache.getItems().isEmpty()) {
                        cartService.flushCartToDatabase(cache);
                    }
                });
                
                flushedCount++;
            } catch (Exception e) {
                log.error("Error flushing cart with key: {}", key, e);
            }
        }
        
        log.info("Scheduled cart flush completed. Processed {} carts", flushedCount);
    }
}

