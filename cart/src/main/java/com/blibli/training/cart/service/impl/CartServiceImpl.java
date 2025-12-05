package com.blibli.training.cart.service.impl;

import com.blibli.training.cart.client.MemberClient;
import com.blibli.training.cart.client.ProductClient;
import com.blibli.training.cart.dto.CartItemResponse;
import com.blibli.training.cart.dto.CartResponse;
import com.blibli.training.cart.entity.Cart;
import com.blibli.training.cart.entity.CartItem;
import com.blibli.training.cart.repository.CartRepository;
import com.blibli.training.cart.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final MemberClient memberClient;
    private final ProductClient productClient;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    @CachePut(value = "carts", key = "#username")
    public CartResponse addToBag(String username, String productName, int quantity) {
        // Validate User
        try {
            memberClient.findByUsername(username);
        } catch (Exception e) {
            throw new RuntimeException("User not found: " + username);
        }

        // Validate Product and Get Price
        Double price;
        try {
            Object productObj = productClient.findByName(productName);
            Map<String, Object> productMap = objectMapper.convertValue(productObj, Map.class);
            price = Double.valueOf(productMap.get("price").toString());
        } catch (Exception e) {
            throw new RuntimeException("Product not found: " + productName);
        }

        // Find or Create Cart
        Cart cart = cartRepository.findByUsername(username)
                .orElseGet(() -> Cart.builder().username(username).build());

        // Find or Create CartItem
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductName().equals(productName))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            item.setPrice(price); // Update price if changed
        } else {
            CartItem newItem = CartItem.builder()
                    .productName(productName)
                    .quantity(quantity)
                    .price(price)
                    .cart(cart)
                    .build();
            cart.getItems().add(newItem);
        }

        Cart savedCart = cartRepository.save(cart);
        return mapToResponse(savedCart);
    }

    @Override
    @Cacheable(value = "carts", key = "#username")
    public CartResponse getAllCart(String username) {
        // Validate User
        try {
            memberClient.findByUsername(username);
        } catch (Exception e) {
            throw new RuntimeException("User not found: " + username);
        }
        // Find Cart in Cart service
        Cart cart = cartRepository.findByUsername(username)
                .orElseGet(() -> {
                    throw new RuntimeException("User " + username + "has no item in cart");
                });

        return mapToResponse(cart);
    }

    private CartResponse mapToResponse(Cart cart) {
        return CartResponse.builder()
                .id(cart.getId())
                .username(cart.getUsername())
                .items(cart.getItems().stream()
                        .map(item -> CartItemResponse.builder()
                                .id(item.getId())
                                .productName(item.getProductName())
                                .quantity(item.getQuantity())
                                .price(item.getPrice())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
