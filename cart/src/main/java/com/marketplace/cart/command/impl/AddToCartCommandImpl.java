package com.marketplace.cart.command.impl;

import com.marketplace.cart.client.ProductServiceClient;
import com.marketplace.cart.command.AddToCartCommand;
import com.marketplace.cart.dto.request.AddToCartCommandRequest;
import com.marketplace.cart.dto.response.CartResponse;
import com.marketplace.cart.dto.response.ProductDetailsResponse;
import com.marketplace.cart.entity.Cart;
import com.marketplace.cart.entity.CartItem;
import com.marketplace.cart.mapper.CartMapper;
import com.marketplace.cart.repository.CartRepository;
import com.marketplace.common.aspect.Auditable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AddToCartCommandImpl implements AddToCartCommand {

    private final CartRepository cartRepository;
    private final ProductServiceClient productServiceClient;

    @Override
    @Transactional
    @Auditable(action = "ADD_TO_CART", description = "Add item to shopping cart")
    public CartResponse execute(AddToCartCommandRequest request) {
        var userId = request.getUserId();
        var addRequest = request.getAddToCartRequest();

        log.info("Adding item to cart for user: {}, product: {}", userId, addRequest.getProductId());

        // Fetch product details from product service
        ProductDetailsResponse product = productServiceClient.getProductById(addRequest.getProductId());
        log.info("Fetched product: {} - {}", product.getId(), product.getName());

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Creating new cart for user: {}", userId);
                    return Cart.builder().userId(userId).build();
                });

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(addRequest.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + addRequest.getQuantity());
            // Update price in case it changed
            item.setPrice(product.getPrice());
            log.info("Updated quantity for product {} to {}", addRequest.getProductId(), item.getQuantity());
        } else {
            CartItem newItem = CartItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .price(product.getPrice())
                    .quantity(addRequest.getQuantity())
                    .build();
            cart.addItem(newItem);
            log.info("Added new item to cart: {}", addRequest.getProductId());
        }

        Cart savedCart = cartRepository.save(cart);
        log.info("Cart saved successfully for user: {}", userId);

        return CartMapper.toCartResponse(savedCart);
    }
}
