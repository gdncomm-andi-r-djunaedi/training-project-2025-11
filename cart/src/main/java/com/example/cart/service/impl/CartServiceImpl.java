package com.example.cart.service.impl;

import com.example.cart.client.ProductClient;
import com.example.cart.dto.*;
import com.example.cart.entity.Cart;
import com.example.cart.entity.CartItem;
import com.example.cart.exceptions.ResourceNotFoundException;
import com.example.cart.repository.CartRepository;
import com.example.cart.service.CartService;
import com.example.cart.utils.APIResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductClient productClient;

    @Override
    public CartResponseDTO getCart(String userId) {

        Cart cart = cartRepository.findById(userId).orElse(null);

        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            log.info("Cart is empty for userId: {}", userId);
            return CartResponseDTO.builder()
                    .userId(userId)
                    .items(Collections.emptyList())
                    .build();
        }

            List<Long> productIds = cart.getItems().stream()
                    .map(CartItem::getProductId)
                    .collect(Collectors.toList());

        List<GetBulkProductResponseDTO> productDetails = new ArrayList<>();
        int batchSize = 20;

        for (int i = 0; i < productIds.size(); i += batchSize) {
            List<Long> batch = productIds.subList(i, Math.min(i + batchSize, productIds.size()));
            try {
                APIResponse<List<GetBulkProductResponseDTO>> response = productClient.fetchProductInBulk(batch).getBody();
                if (response != null && response.getData() != null) {
                    productDetails.addAll(response.getData());
                }
            } catch (Exception e) {
                log.error("Failed to fetch product details for batch: {}", batch, e);
            }
        }

            Map<Long, GetBulkProductResponseDTO> productMap = productDetails.stream()
                    .collect(Collectors.toMap(
                            GetBulkProductResponseDTO::getProductId,
                            Function.identity(),
                            (existing, duplicate) -> existing  // handle duplicates
                    ));

        List<CartItemResponseDTO> itemDTOs = cart.getItems().stream()
                .filter(item -> productMap.containsKey(item.getProductId()))
                .map(item -> {
                    GetBulkProductResponseDTO product = productMap.get(item.getProductId());
                    return CartItemResponseDTO.builder()
                            .productId(item.getProductId())
                            .title(product.getTitle())
                            .price(product.getPrice())
                            .imageUrl(product.getImageUrl())
                            .quantity(item.getQuantity())
                            .build();
                })
                .collect(Collectors.toList());

        log.info("Successfully fetched cart with {} items for userId: {}", itemDTOs.size(), userId);
        return CartResponseDTO.builder()
                .userId(userId)
                .items(itemDTOs)
                .build();

    }

    @Override
    public String addToCartOrUpdateQuantity(String userId, AddToCartRequestDTO request) {
        
        if (request.getProductId() == null) {
            log.error("Invalid productId: productId is null");
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        
        if (request.getQuantity() <= 0) {
            log.error("Invalid quantity: quantity must be positive, got: {}", request.getQuantity());
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        
        Long productId = request.getProductId();
        int quantity = request.getQuantity();

        Cart cart = cartRepository.findById(userId).orElse(new Cart(userId));

        Map<Long, CartItem> itemMap = cart.getItems().stream()
                .collect(Collectors.toMap(CartItem::getProductId, Function.identity()));

        if (itemMap.containsKey(productId)) {
            CartItem existingItem = itemMap.get(productId);
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            cart.getItems().add(new CartItem(productId, quantity));
        }

        cartRepository.save(cart);
        log.info("Successfully added/updated product in cart for userId: {}", userId);
        return "product added to cart successfully";
    }

    @Override
    public String removeItemFromCart(String userId, Long productId) {
        
        if (productId == null) {
            log.error("Invalid productId: productId is null");
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        
        Cart cart = cartRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Cart not found for userId: {}", userId);
                    return new ResourceNotFoundException("Cart not found for user");
                });

        List<CartItem> items = cart.getItems();
        if (items == null || items.isEmpty()) {
            log.warn("Attempted to remove item from empty cart for userId: {}", userId);
            throw new ResourceNotFoundException("Cart is empty");
        }

        boolean removed = items.removeIf(item -> item.getProductId() == (productId));
        if (!removed) {
            log.warn("Product with id {} not found in cart for userId: {}", productId, userId);
            throw new ResourceNotFoundException("Product with id " + productId + " not found in cart");
        }

        cartRepository.save(cart);
        return "product removed from cart successfully";
    }

    @Override
    public void emptyCart(String userId) {
        
        if (userId == null || userId.trim().isEmpty()) {
            log.error("Invalid userId: userId is null or empty");
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        
        Cart cart = cartRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Cart not found for userId: {}", userId);
                    return new ResourceNotFoundException("Cart not found");
                });

        cart.getItems().clear();
        cartRepository.save(cart);
    }

}
