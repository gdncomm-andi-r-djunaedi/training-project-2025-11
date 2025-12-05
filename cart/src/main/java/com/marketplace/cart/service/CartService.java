package com.marketplace.cart.service;

import com.marketplace.cart.cache.CartCache;
import com.marketplace.cart.client.ProductClient;
import com.marketplace.cart.dto.AddToCartRequest;
import com.marketplace.cart.dto.CartResponse;
import com.marketplace.cart.dto.ProductDTO;
import com.marketplace.cart.dto.UpdateCartItemRequest;
import com.marketplace.cart.entity.Cart;
import com.marketplace.cart.entity.CartItem;
import com.marketplace.cart.mapper.CartMapper;
import com.marketplace.cart.repository.CartRepository;
import com.marketplace.common.exception.ResourceNotFoundException;
import com.marketplace.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartCacheService cartCacheService;
    private final CartMapper cartMapper;
    private final ProductClient productClient;

    public CartResponse getCart(UUID memberId) {
        validateMemberId(memberId);
        log.info("Getting cart for member: {}", memberId);

        // Try to get from cache first
        Optional<CartCache> cachedCart = cartCacheService.getCart(memberId);
        if (cachedCart.isPresent()) {
            log.debug("Cart found in cache for member: {}", memberId);
            return cartMapper.mapCacheToResponse(cachedCart.get());
        }

        // Get from database
        Optional<Cart> dbCart = cartRepository.findByMemberIdWithItems(memberId);
        if (dbCart.isPresent()) {
            Cart cart = dbCart.get();
            // Cache the cart
            CartCache cache = convertToCache(cart);
            cartCacheService.saveCart(cache);
            return cartMapper.toResponse(cart);
        }

        // Return empty cart
        return CartResponse.builder()
                .memberId(memberId)
                .items(new ArrayList<>())
                .totalAmount(java.math.BigDecimal.ZERO)
                .totalItems(0)
                .build();
    }

    @Transactional
    public CartResponse addToCart(UUID memberId, AddToCartRequest request) {
        validateMemberId(memberId);
        log.info("Adding product {} to cart for member: {}", request.getProductId(), memberId);

        // Validate product exists and get product details from product-service
        ProductDTO product = productClient.getProductByIdOrThrow(request.getProductId());
        log.debug("Product validated: {} - {}", product.getId(), product.getName());

        // Check if product is active
        if (product.getActive() != null && !product.getActive()) {
            throw new IllegalArgumentException("Product is not available: " + request.getProductId());
        }

        // Get or create cart cache
        CartCache cache = cartCacheService.getCart(memberId)
                .orElse(CartCache.builder()
                        .memberId(memberId)
                        .items(new ArrayList<>())
                        .lastModified(LocalDateTime.now())
                        .build());

        // Check if product already in cart
        Optional<CartCache.CartItemCache> existingItem = cache.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            // Update quantity
            existingItem.get().setQuantity(existingItem.get().getQuantity() + request.getQuantity());
            // Update price in case it changed
            existingItem.get().setPrice(product.getPrice());
            existingItem.get().setProductName(product.getName());
            existingItem.get().setProductImage(product.getFirstImage());
        } else {
            // Add new item with product details from product-service
            CartCache.CartItemCache newItem = CartCache.CartItemCache.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .price(product.getPrice())
                    .quantity(request.getQuantity())
                    .productImage(product.getFirstImage())
                    .build();
            cache.getItems().add(newItem);
        }

        cache.setDirty(true);
        cache.recalculateTotals();
        cartCacheService.saveCart(cache);

        log.info("Product added to cart successfully for member: {}", memberId);
        return cartMapper.mapCacheToResponse(cache);
    }

    @Transactional
    public CartResponse updateCartItem(UUID memberId, String productId, UpdateCartItemRequest request) {
        validateMemberId(memberId);
        log.info("Updating cart item {} for member: {}", productId, memberId);

        // Validate product exists in product-service
        ProductDTO product = productClient.getProductByIdOrThrow(productId);

        CartCache cache = cartCacheService.getCart(memberId)
                .orElseThrow(() -> ResourceNotFoundException.of("Cart", memberId));

        Optional<CartCache.CartItemCache> existingItem = cache.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();

        if (existingItem.isEmpty()) {
            throw ResourceNotFoundException.of("Cart item", productId);
        }

        if (request.getQuantity() <= 0) {
            // Remove item if quantity is 0 or less
            cache.getItems().remove(existingItem.get());
        } else {
            existingItem.get().setQuantity(request.getQuantity());
            // Update price in case it changed
            existingItem.get().setPrice(product.getPrice());
            existingItem.get().setProductName(product.getName());
            existingItem.get().setProductImage(product.getFirstImage());
        }

        cache.setDirty(true);
        cache.recalculateTotals();

        // If cart is empty, delete it
        if (cache.getItems().isEmpty()) {
            deleteCart(memberId);
            return CartResponse.builder()
                    .memberId(memberId)
                    .items(new ArrayList<>())
                    .totalAmount(java.math.BigDecimal.ZERO)
                    .totalItems(0)
                    .build();
        }

        cartCacheService.saveCart(cache);
        return cartMapper.mapCacheToResponse(cache);
    }

    @Transactional
    public CartResponse removeFromCart(UUID memberId, String productId) {
        validateMemberId(memberId);
        log.info("Removing product {} from cart for member: {}", productId, memberId);

        // Validate product exists in product-service (optional, can skip for delete)
        if (!productClient.productExists(productId)) {
            log.warn("Product {} does not exist in product-service, but proceeding with removal from cart", productId);
        }

        CartCache cache = cartCacheService.getCart(memberId)
                .orElseThrow(() -> ResourceNotFoundException.of("Cart", memberId));

        boolean removed = cache.getItems().removeIf(item -> item.getProductId().equals(productId));

        if (!removed) {
            throw ResourceNotFoundException.of("Cart item", productId);
        }

        cache.setDirty(true);
        cache.recalculateTotals();

        // If cart is empty, flush to DB and delete
        if (cache.getItems().isEmpty()) {
            deleteCart(memberId);
            return CartResponse.builder()
                    .memberId(memberId)
                    .items(new ArrayList<>())
                    .totalAmount(java.math.BigDecimal.ZERO)
                    .totalItems(0)
                    .build();
        }

        // Flush to database on delete
        flushCartToDatabase(cache);
        cartCacheService.saveCart(cache);

        return cartMapper.mapCacheToResponse(cache);
    }

    @Transactional
    public void deleteCart(UUID memberId) {
        validateMemberId(memberId);
        log.info("Deleting cart for member: {}", memberId);

        // Delete from database
        cartRepository.findByMemberId(memberId).ifPresent(cartRepository::delete);

        // Delete from cache
        cartCacheService.deleteCart(memberId);
    }

    @Transactional
    public void flushCartToDatabase(CartCache cache) {
        log.info("Flushing cart to database for member: {}", cache.getMemberId());

        Cart cart = cartRepository.findByMemberId(cache.getMemberId())
                .orElse(Cart.builder()
                        .memberId(cache.getMemberId())
                        .items(new ArrayList<>())
                        .build());

        // Clear existing items
        cart.getItems().clear();

        // Add items from cache
        for (CartCache.CartItemCache cacheItem : cache.getItems()) {
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .productId(cacheItem.getProductId())
                    .productName(cacheItem.getProductName())
                    .price(cacheItem.getPrice())
                    .quantity(cacheItem.getQuantity())
                    .productImage(cacheItem.getProductImage())
                    .build();
            cart.getItems().add(item);
        }

        cart.recalculateTotals();
        cartRepository.save(cart);

        // Mark cache as clean
        cache.setDirty(false);
        cartCacheService.saveCart(cache);

        log.info("Cart flushed to database for member: {}", cache.getMemberId());
    }

    private CartCache convertToCache(Cart cart) {
        CartCache cache = CartCache.builder()
                .memberId(cart.getMemberId())
                .items(new ArrayList<>())
                .totalAmount(cart.getTotalAmount())
                .totalItems(cart.getTotalItems())
                .lastModified(cart.getUpdatedAt())
                .dirty(false)
                .build();

        for (CartItem item : cart.getItems()) {
            cache.getItems().add(CartCache.CartItemCache.builder()
                    .productId(item.getProductId())
                    .productName(item.getProductName())
                    .price(item.getPrice())
                    .quantity(item.getQuantity())
                    .productImage(item.getProductImage())
                    .build());
        }

        return cache;
    }

    private void validateMemberId(UUID memberId) {
        if (memberId == null) {
            throw UnauthorizedException.mustLogin();
        }
    }
}
