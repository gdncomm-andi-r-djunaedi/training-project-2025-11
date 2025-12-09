package com.marketplace.cart.service;

import com.marketplace.cart.dto.AddToCartRequest;
import com.marketplace.cart.dto.CartDto;
import com.marketplace.cart.entity.Cart;
import com.marketplace.cart.entity.CartItem;
import com.marketplace.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {
    
    private final CartRepository cartRepository;
    
    public CartDto addToCart(String userId, AddToCartRequest request) {
        log.info("Adding item to cart for user: {}", userId);
        
        // Get or create cart
        Cart cart = cartRepository.findById(userId)
                .orElse(Cart.builder()
                        .userId(userId)
                        .items(new ArrayList<>())
                        .createdAt(LocalDateTime.now())
                        .build());
        
        // Check if product already in cart
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst();
        
        if (existingItem.isPresent()) {
            // Update quantity
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            log.info("Updated quantity for product {} in cart", request.getProductId());
        } else {
            // Add new item
            CartItem newItem = CartItem.builder()
                    .productId(request.getProductId())
                    .productName(request.getProductName())
                    .price(request.getPrice())
                    .quantity(request.getQuantity())
                    .build();
            cart.getItems().add(newItem);
            log.info("Added new product {} to cart", request.getProductId());
        }
        
        cart.setUpdatedAt(LocalDateTime.now());
        Cart savedCart = cartRepository.save(cart);
        
        return convertToDto(savedCart);
    }
    
    public CartDto getCart(String userId) {
        log.info("Getting cart for user: {}", userId);
        
        Cart cart = cartRepository.findById(userId)
                .orElse(Cart.builder()
                        .userId(userId)
                        .items(new ArrayList<>())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build());
        
        return convertToDto(cart);
    }
    
    public CartDto removeFromCart(String userId, String productId) {
        log.info("Removing product {} from cart for user: {}", productId, userId);
        
        Cart cart = cartRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        
        boolean removed = cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        
        if (!removed) {
            throw new RuntimeException("Product not found in cart");
        }
        
        cart.setUpdatedAt(LocalDateTime.now());
        Cart savedCart = cartRepository.save(cart);
        
        log.info("Product removed from cart successfully");
        return convertToDto(savedCart);
    }
    
    public void clearCart(String userId) {
        log.info("Clearing cart for user: {}", userId);
        cartRepository.deleteById(userId);
    }
    
    private CartDto convertToDto(Cart cart) {
        return CartDto.builder()
                .userId(cart.getUserId())
                .items(cart.getItems())
                .totalPrice(cart.getTotalPrice())
                .totalItems(cart.getTotalItems())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }
}
