package com.dev.onlineMarketplace.cart.service;

import com.dev.onlineMarketplace.cart.dto.Product;
import com.dev.onlineMarketplace.cart.model.Cart;
import com.dev.onlineMarketplace.cart.model.CartItem;
import com.dev.onlineMarketplace.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final ProductServiceClient productServiceClient;

    @Cacheable(value = "carts", key = "#userId")
    public Cart getCart(String userId) {
        log.info("Fetching cart for user: {}", userId);
        return cartRepository.findById(userId).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUserId(userId);
            return cartRepository.save(newCart);
        });
    }

    @Transactional
    @CachePut(value = "carts", key = "#userId")
    public Cart addToCart(String userId, String productId, Integer quantity) {
        log.info("Adding item to cart for user: {}, productId: {}, quantity: {}", userId, productId, quantity);
        
        // Fetch product details from product service
        Product product = productServiceClient.getProductById(productId);
        log.info("Fetched product details: id={}, name={}", product.getId(), product.getName());
        
        // Validate stock availability
        if (product.getStock() != null && product.getStock() < quantity) {
            throw new RuntimeException("Insufficient stock. Available: " + product.getStock() + ", Requested: " + quantity);
        }
        
        Cart cart = getCart(userId);

        // Check for existing item - match by either the original productId (SKU/ID) or the product's id
        // This handles cases where productId could be SKU or ID
        String productIdFromResponse = product.getId();
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId) || i.getProductId().equals(productIdFromResponse))
                .findFirst();

        if (existingItem.isPresent()) {
            int newQuantity = existingItem.get().getQuantity() + quantity;
            // Validate stock for updated quantity
            if (product.getStock() != null && product.getStock() < newQuantity) {
                throw new RuntimeException("Insufficient stock. Available: " + product.getStock() + ", Requested: " + newQuantity);
            }
            existingItem.get().setQuantity(newQuantity);
            // Update price in case it changed
            existingItem.get().setPrice(product.getPrice());
            // Update product name in case it changed
            existingItem.get().setProductName(product.getName());
            log.info("Updated existing cart item: productId={}, newQuantity={}", existingItem.get().getProductId(), newQuantity);
        } else {
            // Create new cart item using the original productId (preserves SKU/ID as passed by user)
            CartItem cartItem = new CartItem();
            cartItem.setProductId(productId);
            cartItem.setProductName(product.getName());
            cartItem.setQuantity(quantity);
            cartItem.setPrice(product.getPrice());
            cart.getItems().add(cartItem);
            log.info("Added new cart item: productId={}, quantity={}", productId, quantity);
        }

        cart.calculateTotals();
        return cartRepository.save(cart);
    }

    @Transactional
    @CachePut(value = "carts", key = "#userId")
    public Cart removeFromCart(String userId, String productId) {
        log.info("Removing item {} from cart for user: {}", productId, userId);
        Cart cart = getCart(userId);
        cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        cart.calculateTotals();
        return cartRepository.save(cart);
    }

    @CacheEvict(value = "carts", key = "#userId")
    public void clearCart(String userId) {
        log.info("Clearing cart for user: {}", userId);
        cartRepository.deleteById(userId);
    }
}
