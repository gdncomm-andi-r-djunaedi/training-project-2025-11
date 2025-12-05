package com.gdn.training.cart.controller;

import com.gdn.training.cart.entity.Cart;
import com.gdn.training.cart.entity.CartItem;
import com.gdn.training.cart.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    @Autowired
    private CartRepository cartRepository;

    @GetMapping("/{userId}")
    public Cart getCart(@PathVariable Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    return cartRepository.save(newCart);
                });
    }

    @PostMapping("/{userId}/add")
    public Cart addToCart(@PathVariable Long userId, @RequestBody CartItem item) {
        Cart cart = getCart(userId);
        item.setCart(cart);
        cart.getItems().add(item);
        return cartRepository.save(cart);
    }
}
