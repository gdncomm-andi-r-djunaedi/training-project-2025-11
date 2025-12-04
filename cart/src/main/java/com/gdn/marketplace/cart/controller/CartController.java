package com.gdn.marketplace.cart.controller;

import com.gdn.marketplace.cart.command.AddToCartCommand;
import com.gdn.marketplace.cart.command.GetCartCommand;
import com.gdn.marketplace.cart.command.RemoveFromCartCommand;
import com.gdn.marketplace.cart.entity.Cart;
import com.gdn.marketplace.cart.entity.CartItem;
import com.gdn.marketplace.cart.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private AddToCartCommand addToCartCommand;

    @Autowired
    private GetCartCommand getCartCommand;

    @Autowired
    private RemoveFromCartCommand removeFromCartCommand;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping
    public Cart addToCart(@RequestHeader("Authorization") String token, @RequestBody CartItem item) {
        String username = extractUsername(token);
        return addToCartCommand.execute(new AddToCartCommand.Request(username, item));
    }

    @GetMapping
    public Cart getCart(@RequestHeader("Authorization") String token) {
        String username = extractUsername(token);
        return getCartCommand.execute(username);
    }

    @DeleteMapping("/{productId}")
    public Cart removeFromCart(@RequestHeader("Authorization") String token, @PathVariable String productId) {
        String username = extractUsername(token);
        return removeFromCartCommand.execute(new RemoveFromCartCommand.Request(username, productId));
    }

    private String extractUsername(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.extractUsername(token);
    }
}
