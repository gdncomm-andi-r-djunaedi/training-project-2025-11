package com.example.cart.command;

import com.example.cart.entity.CartItem;
import com.example.cart.repo.CartItemRepository;
import com.example.cart.service.CartCacheService;
import com.example.commandlib.Command;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class ViewCartCommand implements Command<ResponseEntity<?>> {
    private final CartItemRepository repo;
    private final CartCacheService cacheService;
    private final String username;

    public ViewCartCommand(CartItemRepository repo, CartCacheService cacheService, String username) {
        this.repo = repo;
        this.cacheService = cacheService;
        this.username = username;
    }

    @Override
    public ResponseEntity<?> execute() {
        // Try to get from cache first
        List<CartItem> items = cacheService.getCachedCart(username);
        if (items != null) {
            return ResponseEntity.ok(items);
        }
        
        // Cache miss - fetch from MongoDB and cache
        items = repo.findByUsername(username);
        cacheService.cacheCart(username, items);
        return ResponseEntity.ok(items);
    }
}
