package com.marketplace.cart.command;

import com.marketplace.cart.dto.AddToCartRequest;
import com.marketplace.cart.entity.Cart;
import com.marketplace.cart.service.CartService;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class AddToCartCommand implements CartCommand {

    private final CartService cartService;
    private final UUID userId;
    private final AddToCartRequest request;

    @Override
    public Cart execute() {
        return cartService.addToCart(userId, request);
    }
}
