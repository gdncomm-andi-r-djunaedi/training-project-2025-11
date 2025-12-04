package com.blibli.cart.service.impl;

import com.blibli.cart.client.ProductClient;
import com.blibli.cart.dto.*;
import com.blibli.cart.entity.Cart;
import com.blibli.cart.entity.CartItem;
import com.blibli.cart.exception.BadRequestException;
import com.blibli.cart.exception.ExternalServiceException;
import com.blibli.cart.exception.ResourceNotFoundException;
import com.blibli.cart.service.CartService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {
    private static final String CART_KEY_PREFIX = "cart:";
    private static final long CART_TTL_DAYS = 7;

    private final com.blibli.cart.repository.CartRepository cartRepository;  // MongoDB repository
    private final RedisTemplate<String, String> redisTemplate;  // Redis cache
    private final ObjectMapper objectMapper;
    private final ProductClient productClient;


    public CartResponseDTO addToCarts(String userId, AddToCartRequest request) {

        if (request.getProductId() == null || request.getProductId().trim().isEmpty()) {
            throw new BadRequestException("Product ID is required");
        }
        
        ProductResponse product = fetchProductById(request.getProductId());
        validateProduct(product, request.getQuantity());

        Cart cart = getCartFromCache(userId);

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> 
                    item.getProductId().equals(product.getId()) ||
                    item.getSku().equals(product.getSku())
                )
                .findFirst();

        if (existingItem.isPresent()) {
            // Update existing item, fixing existing items
//            CartItem item = existingItem.get();

            int newQuantity = existingItem.get().getQuantity() + request.getQuantity();

            if (product.getStockQuantity() != null && newQuantity > product.getStockQuantity()) {
                throw new BadRequestException("Insufficient stock. Available: " + product.getStockQuantity());
            }
            // old code
            existingItem.get().setQuantity(newQuantity);
            existingItem.get().setPrice(product.getPrice());
        } else {
            CartItem newItem = CartItem.builder()
                    .productId(product.getId())
                    .sku(product.getSku())
                    .name(product.getName())
                    .price(product.getPrice())
                    .quantity(request.getQuantity())
                    .addedAt(LocalDateTime.now())
                    .build();
            cart.getItems().add(newItem);
        }

        cart.setUpdatedAt(LocalDateTime.now());


        // Save to MongoDB first (source of truth), then cache in Redis
        //  old code
        saveCart(userId, cart);

        log.info("Product added to cart for user {}: {}", userId, request.getProductId());

        return mapToResponse(cart);
    }


    @Override
    public CartResponseDTO addToCart(String userId, AddToCartRequest request) {

        if (request.getProductId() == null || request.getProductId().trim().isEmpty()) {
            throw new BadRequestException("Product ID is required");
        }

        ProductResponse product = fetchProductById(request.getProductId());
        validateProduct(product, request.getQuantity());

        Cart cart = getCartFromCache(userId);

        CartItem updatedItem;

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item ->
                        item.getProductId().equals(product.getId()) ||
                                item.getSku().equals(product.getSku())
                )
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();
            item.setQuantity(newQuantity);
            item.setPrice(product.getPrice());
            item.setAddedAt(LocalDateTime.now());
            updatedItem = item;
        } else {
            CartItem newItem = CartItem.builder()
                    .productId(product.getId())
                    .sku(product.getSku())
                    .name(product.getName())
                    .price(product.getPrice())
                    .quantity(request.getQuantity())
                    .addedAt(LocalDateTime.now())
                    .build();
            cart.getItems().add(newItem);
            updatedItem = newItem;
        }

        cart.setUpdatedAt(LocalDateTime.now());

        saveCart(userId, cart);
        cacheCartInRedis(userId, cart);
        return mapToResponse(cart, updatedItem);
    }

    @Override
    public CartResponseDTO getCarts(String userId) {
        // Read-through cache: Check Redis first, fallback to MongoDB
        Cart cart = getCartFromCache(userId);
        boolean updated = false;

        for (CartItem item : cart.getItems()) {
            ProductResponse product = productClient.getProductById(item.getProductId()).getData();
            if (product != null) {
                boolean itemUpdated = false;
                if (!item.getPrice().equals(product.getPrice())) {
                    item.setPrice(product.getPrice());
                    itemUpdated = true;
                }
                if (itemUpdated) {
                    updated = true;
                    item.setAddedAt(LocalDateTime.now());
                }

            }
            }

        if (updated) {
            cart.setUpdatedAt(LocalDateTime.now());
            cartRepository.save(cart);
            cacheCartInRedis(userId, cart);
        }


        return mapToResponse(cart);
    }

    @Override
    public CartResponseDTO removeItemFromCart(String userId, String productId) {
        Cart cart = getCartFromCache(userId);

//        boolean removed = cart.getItems().removeIf(
//                item -> item.getProductId().equals(productId) ||
//                        item.getSku().equals(productId));

        boolean removed = false;
        Iterator<CartItem> iterator=cart.getItems().iterator();
        while (iterator.hasNext()){
            CartItem item=iterator.next();

            if(item.getProductId().equals(productId) || item.getSku().equals(productId)){
                iterator.remove();
                removed=true;
            }
        }

        if (!removed) {
            throw new BadRequestException("Product not found in cart");
        }
        cart.setUpdatedAt(LocalDateTime.now());

        saveCart(userId, cart);

        log.info("Product removed from cart for user {}: {}", userId, productId);
        return mapToResponse(cart);
    }

    @Override
    public void clearCart(String userId) {
        // Delete from both MongoDB and Redis
        log.info("Clearing cart for user: {}", userId);
        
        // Delete from MongoDB
        cartRepository.deleteByUserId(userId);
        
        // Delete from Redis cache
        String key = CART_KEY_PREFIX + userId;
        redisTemplate.delete(key);
        
        log.info("Cart cleared successfully for user: {}", userId);
    }
    private Cart getCartFromCache(String userId) {
        log.debug("Getting cart for user {} (read-through cache)", userId);
        
        // Step 1: Try Redis cache first
        String key = CART_KEY_PREFIX + userId;
        String cartJson = redisTemplate.opsForValue().get(key);

        if (cartJson != null) {
            log.debug("Cache HIT for user {}", userId);
            try {
                return objectMapper.readValue(cartJson, Cart.class);
            } catch (JsonProcessingException e) {
                log.error("Failed to deserialize cart from Redis cache", e);
                // Continue to fetch from MongoDB if Redis data is corrupted
            }
        }

        // Step 2: Cache MISS - Fetch from MongoDB
        log.debug("Cache MISS for user {} - fetching from MongoDB", userId);
        Optional<Cart> cartFromDb = cartRepository.findByUserId(userId);
        
        Cart cart;
        if (cartFromDb.isPresent()) {
            cart = cartFromDb.get();
            log.info("Cart found in MongoDB for user {}", userId);
            
            // Step 3: Cache it in Redis for future requests
            cacheCartInRedis(userId, cart);
        } else {
            // No cart exists - create new empty cart
            log.info("No cart found for user {} - creating new cart", userId);
            cart = Cart.builder()
                    .userId(userId)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
        }

        return cart;
    }

    private void saveCart(String userId, Cart cart) {
        log.debug("Saving cart for user {} (write-through)", userId);
        
// saving in to mongo db
        cart.setUserId(userId);
        if (cart.getCreatedAt() == null) {
            cart.setCreatedAt(LocalDateTime.now());
        }
        cart.setUpdatedAt(LocalDateTime.now());
        
        Cart savedCart = cartRepository.save(cart);
        log.info("Cart saved to MongoDB for user {}", userId);
        
// update redis
        cacheCartInRedis(userId, savedCart);
    }


    private void cacheCartInRedis(String userId, Cart cart) {
        String key = CART_KEY_PREFIX + userId;
        try {
            String cartJson = objectMapper.writeValueAsString(cart);
            redisTemplate.opsForValue().set(key, cartJson, CART_TTL_DAYS, TimeUnit.DAYS);
            log.debug("Cart cached in Redis for user {} with TTL {} days", userId, CART_TTL_DAYS);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize cart for Redis cache", e);
        }
    }

    private CartResponseDTO mapToResponse(Cart cart) {
        List<CartItemResponseDTO> items = cart.getItems().stream()
                .map(this::mapItemToResponse)
                .collect(Collectors.toList());

        BigDecimal totalAmount = items.stream()
                .map(CartItemResponseDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer totalItems = cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        return CartResponseDTO.builder()
                .userId(cart.getUserId())
                .items(items)
                .totalAmount(totalAmount)
                .totalItems(totalItems)
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    public CartResponseDTO mapToResponse(Cart cart, CartItem onlyThisItem) {

        CartResponseDTO response = new CartResponseDTO();
        response.setUserId(cart.getUserId());
        response.setUpdatedAt(cart.getUpdatedAt());

        // Instead of using cart.getItems(), return only updated item
        response.setItems(List.of(
                CartItemResponseDTO.builder()
                        .productId(onlyThisItem.getProductId())
                        .sku(onlyThisItem.getSku())
                        .name(onlyThisItem.getName())
                        .price(onlyThisItem.getPrice())
                        .quantity(onlyThisItem.getQuantity())
                        .addedAt(onlyThisItem.getAddedAt())
                        .build()
        ));

        return response;
    }


    private CartItemResponseDTO mapItemToResponse(CartItem item) {
        BigDecimal subtotal = item.getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        return CartItemResponseDTO.builder()
                .productId(item.getProductId())
                .sku(item.getSku())
                .name(item.getName())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .subtotal(subtotal)
                .addedAt(item.getAddedAt())
                .build();
    }

    private ProductResponse fetchProductById(String productId) {
        try {
            log.debug("Fetching product details for productId: {}", productId);
//            ProductResponse product;
            ApiResponse<ProductResponse> apiResponse  = productClient.getProductById(productId);
            ProductResponse product = apiResponse.getData();
            if (product == null) {
                log.error("Product not found: {}", productId);
                throw new ResourceNotFoundException("Product not found with id: " + productId);
            }
            
            return product;
        } catch (FeignException.NotFound e) {
            log.error("Product not found in product service: {}", productId);
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        } catch (FeignException e) {
            log.error("Error communicating with product service: {}", e.getMessage(), e);
            throw new ExternalServiceException("Failed to fetch product details. Please try again later.");
        } catch (Exception e) {
            log.error("Unexpected error while fetching product: {}", e.getMessage(), e);
            throw new ExternalServiceException("An unexpected error occurred. Please try again later.");
        }
    }

    private void validateProduct(ProductResponse product, Integer requestedQuantity) {
        // Check stock availability (changed from getStock() to getStockQuantity())
        if (product.getStockQuantity() != null && product.getStockQuantity() < requestedQuantity) {
            log.warn("Insufficient stock for product {}: requested={}, available={}", 
                    product.getId(), requestedQuantity, product.getStockQuantity());
            throw new BadRequestException(
                    String.format("Insufficient stock. Only %d items available", product.getStockQuantity()));
        }

        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Invalid price for product: {}", product.getId());
            throw new BadRequestException("Invalid product price");
        }
    }

}
