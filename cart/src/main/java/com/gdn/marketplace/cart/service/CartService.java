package com.gdn.marketplace.cart.service;

import com.gdn.marketplace.cart.entity.Cart;
import com.gdn.marketplace.cart.entity.CartItem;
import com.gdn.marketplace.cart.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartRepository repository;

    public Cart addToCart(String username, CartItem item) {
        Cart cart = repository.findByUsername(username).orElse(new Cart());
        if (cart.getUsername() == null) {
            cart.setUsername(username);
            cart.setTotalPrice(BigDecimal.ZERO);
        }

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(item.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + item.getQuantity());
        } else {
            cart.getItems().add(item);
        }

        recalculateTotal(cart);
        return repository.save(cart);
    }

    public Cart getCart(String username) {
        return repository.findByUsername(username).orElse(new Cart());
    }

    public Cart removeFromCart(String username, String productId) {
        Cart cart = repository.findByUsername(username).orElse(null);
        if (cart != null) {
            cart.getItems().removeIf(item -> item.getProductId().equals(productId));
            recalculateTotal(cart);
            return repository.save(cart);
        }
        return null;
    }

    private void recalculateTotal(Cart cart) {
        BigDecimal total = cart.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotalPrice(total);
    }
}
