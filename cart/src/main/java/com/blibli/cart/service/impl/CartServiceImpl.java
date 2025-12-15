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
import feign.codec.DecodeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
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

    @Override
    public CartResponseDTO addToCart(String userId, AddToCartRequest request) {

        if (request.getProductId() == null || request.getProductId().trim().isEmpty()) {
            throw new BadRequestException("Product ID is required");
        }

        // Validate SKU format if provided (optional field)
        if (request.getSku() != null && !request.getSku().trim().isEmpty()) {
            validateSkuFormat(request.getSku());
        }

        // Validate quantity
        if (request.getQuantity() <= 0) {
            throw new BadRequestException("Quantity must be greater than zero");
        }

        // feign call from product service
        ProductResponse product = fetchProductById(request.getProductId());
        //checking price and quantity in product service



        // fix cart issue with invalid sku


        if (request.getSku() != null && !request.getSku().trim().isEmpty()) {
            String providedSku = request.getSku().trim().toUpperCase();
            String productSku = product.getSku() != null ? product.getSku().trim().toUpperCase() : null;

            if (productSku == null) {
                log.warn("Product {} does not have a SKU", product.getId());
                throw new BadRequestException("Product does not have a SKU assigned");
            }

            if (!providedSku.equals(productSku)) {
                log.warn("SKU mismatch for product {}: provided SKU '{}' does not match product SKU '{}'",
                        product.getId(), providedSku, productSku);
                throw new BadRequestException(
                        String.format("Invalid SKU. Provided SKU '%s' does not match product SKU '%s'",
                                providedSku, productSku));
            }

            log.debug("SKU validated successfully for product {}: {}", product.getId(), providedSku);
        }


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
            item.setAddedAt(new Date());
            updatedItem = item;
        } else {
            CartItem newItem = CartItem.builder()
                    .productId(product.getId())
                    .sku(product.getSku())
                    .name(product.getName())
                    .price(product.getPrice())
                    .quantity(request.getQuantity())
                    .addedAt(new Date())
                    .build();
            cart.getItems().add(newItem);
            updatedItem = newItem;
        }

        cart.setUpdatedAt(new Date());

        saveCart(userId, cart);
//        cacheCartInRedis(userId, cart);
        return mapToResponse(cart, updatedItem);
    }

    @Override
    public CartResponseDTO getCarts(String userId) {
        // Read-through cache: Check Redis first, fallback to MongoDB
        Cart cart = getCartFromCache(userId);
        boolean updated = false;

//        for (CartItem item : cart.getItems()) {
//            ProductResponse product = productClient.getProductById(item.getProductId()).getData();
//            if (product != null) {
//                boolean itemUpdated = false;
//                if (!item.getPrice().equals(product.getPrice())) {
//                    item.setPrice(product.getPrice());
//                    itemUpdated = true;
//                }
//                if (itemUpdated) {
//                    updated = true;
//                    item.setAddedAt(new Date());
//                }
//
//            }
//            }

        // Use iterator to safely remove items during iteration
        Iterator<CartItem> iterator = cart.getItems().iterator();

        while (iterator.hasNext()) {
            CartItem item = iterator.next();

            try {
                ProductResponse product = productClient.getProductById(item.getProductId()).getData();

                if (product == null) {
                    // Product deleted - remove from cart
                    log.warn("Product {} not found, removing from cart", item.getProductId());
                    iterator.remove();
                    updated = true;
                    continue;
                }

                // heck stock availability
                if (product.getStockQuantity() != null && product.getStockQuantity() <= 0) {
                    // Product out of stock - remove from cart
                    log.warn("Product {} out of stock, removing from cart", item.getProductId());
                    iterator.remove();
                    updated = true;
                    continue;
                }

                // just quantity if exceeds available stock
                if (product.getStockQuantity() != null && item.getQuantity() > product.getStockQuantity()) {
                    log.warn("Cart quantity {} exceeds available stock {} for product {}, adjusting quantity",
                            item.getQuantity(), product.getStockQuantity(), item.getProductId());
                    item.setQuantity(product.getStockQuantity());
                    updated = true;
                }

                //Update price if changed
                if (product.getPrice() != null) {
                    if (item.getPrice() == null ||
                            item.getPrice().compareTo(product.getPrice()) != 0) {
                        log.info("Price updated for product {}: {} -> {}",
                                item.getProductId(), item.getPrice(), product.getPrice());
                        item.setPrice(product.getPrice());
                        updated = true;
                    }
                }

                //Update product metadata (name, SKU)
                if (product.getName() != null && !product.getName().equals(item.getName())) {
                    item.setName(product.getName());
                    updated = true;
                }

                if (product.getSku() != null && !product.getSku().equals(item.getSku())) {
                    item.setSku(product.getSku());
                    updated = true;
                }

                // Update addedAt timestamp if any changes were made
                if (updated) {
                    item.setAddedAt(new Date());
                }

            } catch (Exception e) {
                log.error("Failed to fetch product {} for cart sync: {}",
                        item.getProductId(), e.getMessage());
            }
        }

        if (updated) {
            cart.setUpdatedAt(new Date());
            cartRepository.save(cart);
            cacheCartInRedis(userId, cart);
        }
        else {
            refreshCartTTL(userId);
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
        cart.setUpdatedAt(new Date());

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

        String key = CART_KEY_PREFIX + userId;
        String cartJson = redisTemplate.opsForValue().get(key);

        if (cartJson != null) {
            log.debug("Cache HIT for user {}", userId);
            try {
                return objectMapper.readValue(cartJson, Cart.class);
            } catch (JsonProcessingException e) {
                log.error("Failed to deserialize cart from Redis cache", e);
            }
        }

        //  Cache MISS - Fetch from MongoDB
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
                    .createdAt(new Date())
                    .updatedAt(new Date())
                    .build();
        }

        return cart;
    }

    private void saveCart(String userId, Cart cart) {
        log.debug("Saving cart for user {} (write-through)", userId);
        
// saving in to mongo db
        cart.setUserId(userId);
        if (cart.getCreatedAt() == null) {
            cart.setCreatedAt(new Date());
        }
        cart.setUpdatedAt(new Date());
        
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

    private void refreshCartTTL(String userId) {
        String key = CART_KEY_PREFIX + userId;
        redisTemplate.expire(key, Duration.ofHours(24));
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
                .updatedAt(new Date())
                .build();
    }

    public CartResponseDTO mapToResponse(Cart cart, CartItem onlyThisItem) {
        // Return only the added/updated item (not the entire cart)
        CartItemResponseDTO itemResponse = mapItemToResponse(onlyThisItem);

        List<CartItemResponseDTO> items = List.of(itemResponse);

        // Calculate totals from the single item
        BigDecimal totalAmount = itemResponse.getSubtotal();
        Integer totalItems = onlyThisItem.getQuantity();

        return CartResponseDTO.builder()
                .userId(cart.getUserId())
                .items(items)
                .totalAmount(totalAmount)
                .totalItems(totalItems)
                .updatedAt(cart.getUpdatedAt())
                .build();
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
            ApiResponse<ProductResponse> apiResponse = productClient.getProductById(productId);
            
            if (apiResponse == null) {
                log.error("Null response from product service for productId: {}", productId);
                throw new ExternalServiceException("Invalid response from product service");
            }
            
            ProductResponse product = apiResponse.getData();
            if (product == null) {
                log.error("Product not found: {}", productId);
                throw new ResourceNotFoundException("Product not found with id: " + productId);
            }
            
            return product;
        } catch (FeignException.NotFound e) {
            log.error("Product not found (404) in product service: {} - Response: {}", 
                productId, e.contentUTF8());
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        } catch (DecodeException e) {
            log.error("Failed to decode product service response for productId: {} - Error: {}", 
                productId, e.getMessage(), e);
            throw new ExternalServiceException(
                "Invalid product data received from product service. Product ID may be invalid: " + productId);
        } catch (FeignException e) {
            log.error("Feign error communicating with product service: productId={}, status={}, message={}, response={}", 
                productId, e.status(), e.getMessage(), e.contentUTF8(), e);
            
            // Provide more specific error messages based on status code
            if (e.status() == 400) {
                throw new BadRequestException("Invalid product ID format: " + productId);
            } else if (e.status() == 404) {
                throw new ResourceNotFoundException("Product not found with id: " + productId);
            } else if (e.status() >= 500) {
                throw new ExternalServiceException("Product service is temporarily unavailable. Please try again later.");
            } else {
                throw new ExternalServiceException("Failed to fetch product details. Please try again later.");
            }
        } catch (ResourceNotFoundException e) {
            // Re-throw ResourceNotFoundException as-is
            throw e;
        } catch (BadRequestException e) {
            // Re-throw BadRequestException as-is
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while fetching product: productId={}, error={}", 
                productId, e.getMessage(), e);
            throw new ExternalServiceException("An unexpected error occurred while fetching product details. Please try again later.");
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

    private void validateSkuFormat(String sku) {
        if (sku == null || sku.trim().isEmpty()) {
            return; // SKU is optional, so empty is OK
        }
        
        String skuRegex = "^[A-Za-z]{3}-\\d{5}-\\d{5}$"; // AAA-#####-#####
        if (!sku.matches(skuRegex)) {
            throw new BadRequestException(
                String.format("Invalid SKU format: '%s'. SKU must match pattern: AAA-#####-##### (e.g., ABC-12345-67890)", sku));
        }
    }

}
