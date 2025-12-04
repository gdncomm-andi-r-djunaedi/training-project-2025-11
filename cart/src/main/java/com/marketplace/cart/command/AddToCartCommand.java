package com.marketplace.cart.command;

import com.marketplace.cart.dto.request.AddToCartCommandRequest;
import com.marketplace.cart.entity.Cart;
import com.marketplace.cart.service.CartService;
import com.marketplace.common.command.Command;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AddToCartCommand implements Command<AddToCartCommandRequest, Cart> {

    private final CartService cartService;

    @Override
    public Cart execute(AddToCartCommandRequest request) {
        return cartService.addToCart(request.getUserId(), request.getAddToCartRequest());
    }
}
