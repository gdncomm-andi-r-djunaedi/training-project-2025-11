package com.marketplace.cart.command;

import com.marketplace.cart.dto.request.RemoveFromCartRequest;
import com.marketplace.cart.dto.response.CartResponse;
import com.marketplace.cart.entity.Cart;
import com.marketplace.cart.mapper.CartMapper;
import com.marketplace.cart.service.CartService;
import com.marketplace.common.command.Command;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RemoveFromCartCommand implements Command<RemoveFromCartRequest, CartResponse> {

    private final CartService cartService;

    @Override
    public CartResponse execute(RemoveFromCartRequest request) {
        Cart cart = cartService.removeFromCart(request.getUserId(), request.getProductId());
        return CartMapper.toCartResponse(cart);
    }
}
