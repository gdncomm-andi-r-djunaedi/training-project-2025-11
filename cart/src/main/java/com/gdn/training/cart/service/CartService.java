package com.gdn.training.cart.service;

import com.gdn.training.cart.client.ProductClient;
import com.gdn.training.cart.dto.ProductDTO;
import com.gdn.training.cart.entity.Cart;
import com.gdn.training.cart.entity.CartItem;
import com.gdn.training.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductClient productClient;

    public Cart getCart(String userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder().userId(userId).build();
                    return cartRepository.save(newCart);
                });
    }

    public Cart addToCart(String userId, CartItem item) {
        // Validate product stock
        ProductDTO product = productClient.getProductById(item.getProductId());
        if (product == null) {
            throw new IllegalArgumentException("Product not found");
        }
        
        Cart cart = getCart(userId);
        
        // Calculate total quantity in cart
        int currentQuantityInCart = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(item.getProductId()))
                .mapToInt(CartItem::getQuantity)
                .sum();
        
        int totalQuantity = currentQuantityInCart + item.getQuantity();
        
        if (totalQuantity > product.getQuantity()) {
            throw new IllegalArgumentException("Insufficient stock. Available: " + product.getQuantity() + ", Requested: " + totalQuantity);
        }
        
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(item.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + item.getQuantity());
        } else {
            cart.getItems().add(item);
        }

        return cartRepository.save(cart);
    }

    public Cart removeFromCart(String userId, String productId) {
        Cart cart = getCart(userId);
        cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        return cartRepository.save(cart);
    }
}
