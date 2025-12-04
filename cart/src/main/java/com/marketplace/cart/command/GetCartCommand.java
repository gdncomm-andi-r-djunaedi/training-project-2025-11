package com.marketplace.cart.command;

import com.marketplace.cart.dto.request.GetCartRequest;
import com.marketplace.cart.entity.Cart;
import com.marketplace.cart.service.CartService;
import com.marketplace.common.command.Command;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetCartCommand implements Command<GetCartRequest, Cart> {

    private final CartService cartService;

    @Override
    public Cart execute(GetCartRequest request) {
        return cartService.getCart(request.getUserId());
    }
}
