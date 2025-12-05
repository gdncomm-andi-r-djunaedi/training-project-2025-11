package com.example.cart.command;

import com.example.cart.entity.CartItem;
import com.example.cart.repo.CartItemRepository;
import com.example.cart.service.CartCacheService;
import com.example.commandlib.Command;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public class AddToCartCommand implements Command<ResponseEntity<?>> {
    private final CartItemRepository repo;
    private final CartCacheService cacheService;
    private final CartItem cartItem;
    private final String username;

    public AddToCartCommand(CartItemRepository repo, CartCacheService cacheService, CartItem cartItem, String username) {
        this.repo = repo;
        this.cacheService = cacheService;
        this.cartItem = cartItem;
        this.username = username;
    }

    @Override
    public ResponseEntity<?> execute() {
        cartItem.setUsername(username);
        repo.save(cartItem);
        // Invalidate cache so next read fetches fresh data
        cacheService.invalidateCache(username);
        return ResponseEntity.ok(Map.of("message", "added"));
    }
}
