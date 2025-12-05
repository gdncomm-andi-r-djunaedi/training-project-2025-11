package com.example.marketplace.cart.controller;

import com.example.marketplace.cart.entity.CartItem;
import com.example.marketplace.cart.service.CartService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService service;

    public CartController(CartService service) {
        this.service = service;
    }

    @PostMapping("/add")
    public CartItem add(@RequestHeader("X-USER-ID") String userId,
                        @RequestParam String productId,
                        @RequestParam int quantity) {
        return service.addToCart(userId, productId, quantity);
    }

    @GetMapping("/view")
    public List<CartItem> view(@RequestHeader("X-USER-ID") String userId) {
        return service.viewCart(userId);
    }

    @DeleteMapping("/delete")
    public void delete(@RequestHeader("X-USER-ID") String userId,
                       @RequestParam String productId) {
        service.deleteItem(userId, productId);
    }
}
