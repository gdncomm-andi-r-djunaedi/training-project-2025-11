package com.marketplace.cart.command;

import com.marketplace.cart.dto.request.GetCartRequest;
import com.marketplace.cart.dto.response.CartResponse;
import com.marketplace.cart.entity.Cart;
import com.marketplace.cart.mapper.CartMapper;
import com.marketplace.cart.service.CartService;
import com.marketplace.common.command.Command;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetCartCommand implements Command<GetCartRequest, CartResponse> {

    private final CartService cartService;

    @Override
    public CartResponse execute(GetCartRequest request) {
        Cart cart = cartService.getCart(request.getUserId());
        return CartMapper.toCartResponse(cart);
    }
}
