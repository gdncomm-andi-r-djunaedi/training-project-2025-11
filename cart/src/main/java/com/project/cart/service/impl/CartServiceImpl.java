package com.project.cart.service.impl;

import com.project.cart.client.ProductServiceClient;
import com.project.cart.dto.ProductDto;
import com.project.cart.dto.request.AddCartItemRequest;
import com.project.cart.dto.request.UpdateCartItemRequest;
import com.project.cart.dto.response.CartCountResponse;
import com.project.cart.dto.response.CartItemResponse;
import com.project.cart.dto.response.CartResponse;
import com.project.cart.entity.Cart;
import com.project.cart.entity.CartItem;
import com.project.cart.entity.CartMetadata;
import com.project.cart.entity.CartStatus;
import com.project.cart.exception.CartLimitExceededException;
import com.project.cart.exception.ProductNotFoundException;
import com.project.cart.repository.CartRepository;
import com.project.cart.service.CartService;
import com.project.cart.service.RedisCartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of CartService
 * Manages cart operations with Redis (primary) and MongoDB (backup)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final RedisCartService redisCartService;
    private final CartRepository cartRepository;
    private final ProductServiceClient productServiceClient;

    @Value("${cart.max-items}")
    private int maxItems;

    @Value("${cart.max-quantity-per-item}")
    private int maxQuantityPerItem;

    @Value("${cart.ttl-days}")
    private int ttlDays;

    @Override
    @Transactional
    public CartResponse addItem(String userId, AddCartItemRequest request) {
        log.info("Adding item to cart: userId={}, productId={}", userId, request.getProductId());

        // Validate quantity
        if (request.getQuantity() > maxQuantityPerItem) {
            throw new CartLimitExceededException(
                    "Quantity exceeds maximum allowed: " + maxQuantityPerItem);
        }

        // Fetch product details
        ProductDto product = productServiceClient.getProductById(request.getProductId());

        if (product == null || !Boolean.TRUE.equals(product.getIsActive())) {
            throw new ProductNotFoundException("Product not found or inactive: " + request.getProductId());
        }

        // Check cart item limit
        long currentItemCount = redisCartService.getItemCount(userId);
        CartItem existingItem = redisCartService.getItem(userId, request.getProductId());

        if (existingItem == null && currentItemCount >= maxItems) {
            throw new CartLimitExceededException("Cart item limit exceeded: " + maxItems);
        }

        // Create or update cart item
        CartItem cartItem;
        if (existingItem != null) {
            // Update existing item
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            if (newQuantity > maxQuantityPerItem) {
                throw new CartLimitExceededException(
                        "Total quantity exceeds maximum: " + maxQuantityPerItem);
            }
            existingItem.setQuantity(newQuantity);
            existingItem.setSubtotal(product.getPrice().multiply(new BigDecimal(newQuantity)));
            cartItem = existingItem;
        } else {
            // Create new item
            cartItem = CartItem.builder()
                    .productId(product.getId())
                    .productSku(product.getSku())
                    .productName(product.getName())
                    .productImage(product.getImages() != null ? product.getImages().getThumbnail() : null)
                    .quantity(request.getQuantity())
                    .price(product.getPrice())
                    .subtotal(product.getPrice().multiply(new BigDecimal(request.getQuantity())))
                    .addedAt(LocalDateTime.now())
                    .build();
        }

        // Save to Redis
        redisCartService.addItem(userId, cartItem);

        // Async save to MongoDB
        syncCartToMongoDB(userId);

        log.info("Item added to cart successfully: userId={}, productId={}",
                userId, request.getProductId());

        return getCart(userId);
    }

    @Override
    @Transactional
    public CartResponse updateItemQuantity(String userId, String productId,
                                           UpdateCartItemRequest request) {
        log.info("Updating item quantity: userId={}, productId={}, quantity={}",
                userId, productId, request.getQuantity());

        // Validate quantity
        if (request.getQuantity() > maxQuantityPerItem) {
            throw new CartLimitExceededException(
                    "Quantity exceeds maximum allowed: " + maxQuantityPerItem);
        }

        // Check if item exists
        CartItem item = redisCartService.getItem(userId, productId);
        if (item == null) {
            throw new ProductNotFoundException("Product not found in cart: " + productId);
        }

        // Update quantity
        redisCartService.updateItemQuantity(userId, productId, request.getQuantity());

        // Async save to MongoDB
        syncCartToMongoDB(userId);

        log.info("Item quantity updated successfully");
        return getCart(userId);
    }

    @Override
    @Transactional
    public CartResponse removeItem(String userId, String productId) {
        log.info("Removing item from cart: userId={}, productId={}", userId, productId);

        // Remove from Redis
        redisCartService.removeItem(userId, productId);

        // Async save to MongoDB
        syncCartToMongoDB(userId);

        log.info("Item removed from cart successfully");
        return getCart(userId);
    }

    @Override
    public CartResponse getCart(String userId) {
        log.info("Fetching cart: userId={}", userId);

        // Try Redis first
        List<CartItem> items = redisCartService.getAllItems(userId);

        // If not in Redis, try MongoDB
        if (items.isEmpty()) {
            Cart cart = cartRepository.findByUserId(userId).orElse(null);
            if (cart != null) {
                items = cart.getItems();
                // Populate Redis
                for (CartItem item : items) {
                    redisCartService.addItem(userId, item);
                }
            }
        }

        // Calculate totals
        int totalItems = items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        BigDecimal totalAmount = items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Convert to response
        List<CartItemResponse> itemResponses = items.stream()
                .map(this::convertToItemResponse)
                .collect(Collectors.toList());

        return CartResponse.builder()
                .cartId(userId)
                .userId(userId)
                .items(itemResponses)
                .totalItems(totalItems)
                .totalAmount(totalAmount)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional
    public void clearCart(String userId) {
        log.info("Clearing cart: userId={}", userId);

        // Clear Redis
        redisCartService.clearCart(userId);

        // Clear MongoDB
        cartRepository.findByUserId(userId)
                .ifPresent(cartRepository::delete);

        log.info("Cart cleared successfully");
    }

    @Override
    public CartCountResponse getCartCount(String userId) {
        log.info("Fetching cart count: userId={}", userId);

        List<CartItem> items = redisCartService.getAllItems(userId);
        int count = items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        return CartCountResponse.builder()
                .count(count)
                .build();
    }

    /**
     * Async sync cart to MongoDB
     */
    @Async
    public void syncCartToMongoDB(String userId) {
        log.debug("Syncing cart to MongoDB: userId={}", userId);

        try {
            List<CartItem> items = redisCartService.getAllItems(userId);

            if (items.isEmpty()) {
                // Remove cart if empty
                cartRepository.findByUserId(userId)
                        .ifPresent(cartRepository::delete);
                return;
            }

            // Calculate totals
            int totalItems = items.stream()
                    .mapToInt(CartItem::getQuantity)
                    .sum();

            BigDecimal totalAmount = items.stream()
                    .map(CartItem::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Find or create cart
            Cart cart = cartRepository.findByUserId(userId)
                    .orElse(Cart.builder()
                            .userId(userId)
                            .status(CartStatus.ACTIVE)
                            .createdAt(LocalDateTime.now())
                            .metadata(CartMetadata.builder()
                                    .lastActivity(LocalDateTime.now())
                                    .build())
                            .build());

            // Update cart
            cart.setItems(items);
            cart.setTotalItems(totalItems);
            cart.setTotalAmount(totalAmount);
            cart.setUpdatedAt(LocalDateTime.now());
            cart.setExpiresAt(LocalDateTime.now().plusDays(ttlDays));
            cart.getMetadata().setLastActivity(LocalDateTime.now());

            cartRepository.save(cart);
            log.debug("Cart synced to MongoDB successfully");
        } catch (Exception e) {
            log.error("Error syncing cart to MongoDB: {}", e.getMessage(), e);
        }
    }

    /**
     * Convert CartItem to CartItemResponse
     */
    private CartItemResponse convertToItemResponse(CartItem item) {
        return CartItemResponse.builder()
                .productId(item.getProductId())
                .productSku(item.getProductSku())
                .productName(item.getProductName())
                .productImage(item.getProductImage())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .subtotal(item.getSubtotal())
                .addedAt(item.getAddedAt())
                .build();
    }
}
