package com.marketplace.cart.command;

import com.marketplace.cart.dto.request.AddToCartCommandRequest;
import com.marketplace.cart.dto.response.CartResponse;
import com.marketplace.cart.entity.Cart;
import com.marketplace.cart.mapper.CartMapper;
import com.marketplace.cart.service.CartService;
import com.marketplace.common.command.Command;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AddToCartCommand implements Command<AddToCartCommandRequest, CartResponse> {

    private final CartService cartService;

    @Override
    public CartResponse execute(AddToCartCommandRequest request) {
        Cart cart = cartService.addToCart(request.getUserId(), request.getAddToCartRequest());
        return CartMapper.toCartResponse(cart);
    }
}
