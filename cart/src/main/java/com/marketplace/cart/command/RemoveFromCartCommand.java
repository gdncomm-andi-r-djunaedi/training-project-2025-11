package com.marketplace.cart.command;

import com.marketplace.cart.entity.Cart;
import com.marketplace.cart.service.CartService;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class RemoveFromCartCommand implements CartCommand {

    private final CartService cartService;
    private final UUID userId;
    private final String productId;

    @Override
    public Cart execute() {
        return cartService.removeFromCart(userId, productId);
    }
}
