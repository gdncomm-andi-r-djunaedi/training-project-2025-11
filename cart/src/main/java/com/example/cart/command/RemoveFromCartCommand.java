package com.example.cart.command;

import com.example.cart.repo.CartItemRepository;
import com.example.cart.service.CartCacheService;
import com.example.commandlib.Command;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public class RemoveFromCartCommand implements Command<ResponseEntity<?>> {
    private final CartItemRepository repo;
    private final CartCacheService cacheService;
    private final String username;
    private final String sku;

    public RemoveFromCartCommand(CartItemRepository repo, CartCacheService cacheService, String username, String sku) {
        this.repo = repo;
        this.cacheService = cacheService;
        this.username = username;
        this.sku = sku;
    }

    @Override
    public ResponseEntity<?> execute() {
        repo.deleteByUsernameAndSku(username, sku);
        // Invalidate cache
        cacheService.invalidateCache(username);
        return ResponseEntity.ok(Map.of("message", "removed"));
    }
}
