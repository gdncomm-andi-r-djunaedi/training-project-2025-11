package com.ecommerce.cart.service;

import com.ecommerce.cart.entity.CartItem;
import com.ecommerce.cart.repository.CartRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public void addToCart(String username, String productId, int quantity) {
        Optional<CartItem> existingItem = cartRepository.findByUsernameAndProductId(username, productId);
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartRepository.save(item);
        } else {
            CartItem item = new CartItem();
            item.setUsername(username);
            item.setProductId(productId);
            item.setQuantity(quantity);
            cartRepository.save(item);
        }
    }

    public List<CartItem> viewCart(String username) {
        return cartRepository.findByUsername(username);
    }

    public void deleteFromCart(String username, Long cartItemId) {
        CartItem item = cartRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!item.getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized access to cart item");
        }

        cartRepository.delete(item);
    }
}
