package com.gdn.training.cart.service;

import com.gdn.training.cart.dto.AddToCartRequest;
import com.gdn.training.cart.entity.Cart;
import com.gdn.training.cart.entity.CartItem;
import com.gdn.training.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.training.cart.dto.CartDto;
import com.gdn.training.cart.dto.CartItemDto;
import com.gdn.training.cart.dto.GetCartResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;

    @Transactional
    public Cart addToCart(String username, AddToCartRequest request) {
        Cart cart = cartRepository.findByUsername(username)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .username(username)
                            .items(new ArrayList<>())
                            .build();
                    return cartRepository.save(newCart);
                });

        // Check if product already exists in cart
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .productId(request.getProductId())
                    .quantity(request.getQuantity())
                    .build();
            cart.getItems().add(newItem);
        }

        return cartRepository.save(cart);
    }

    @Transactional(readOnly = true)
    public GetCartResponse getCart(String username) {
        Cart cart = cartRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        List<CartItemDto> itemDtos = cart.getItems().stream()
                .map(item -> CartItemDto.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        CartDto cartDto = CartDto.builder()
                .id(cart.getId())
                .username(cart.getUsername())
                .items(itemDtos)
                .build();

        return GetCartResponse.builder()
                .id(cart.getId())
                .cart(cartDto)
                .build();
    }

    @Transactional
    public Cart removeFromCart(String username, String productId) {
        Cart cart = cartRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        // Remove the entry of those product_id that related with the username
        boolean removed = cart.getItems().removeIf(item -> item.getProductId().equals(productId));

        return cartRepository.save(cart);
    }
}
