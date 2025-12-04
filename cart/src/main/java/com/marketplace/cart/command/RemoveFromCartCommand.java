package com.marketplace.cart.command;

import com.marketplace.cart.dto.request.RemoveFromCartRequest;
import com.marketplace.cart.entity.Cart;
import com.marketplace.cart.service.CartService;
import com.marketplace.common.command.Command;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RemoveFromCartCommand implements Command<RemoveFromCartRequest, Cart> {

    private final CartService cartService;

    @Override
    public Cart execute(RemoveFromCartRequest request) {
        return cartService.removeFromCart(request.getUserId(), request.getProductId());
    }
}
