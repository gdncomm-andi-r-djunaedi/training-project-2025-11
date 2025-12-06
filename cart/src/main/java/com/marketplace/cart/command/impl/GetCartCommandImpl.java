package com.marketplace.cart.command.impl;

import com.marketplace.cart.command.GetCartCommand;
import com.marketplace.cart.dto.request.GetCartRequest;
import com.marketplace.cart.dto.response.CartResponse;
import com.marketplace.cart.entity.Cart;
import com.marketplace.cart.mapper.CartMapper;
import com.marketplace.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetCartCommandImpl implements GetCartCommand {

    private final CartRepository cartRepository;

    @Override
    public CartResponse execute(GetCartRequest request) {
        var userId = request.getUserId();
        
        log.info("Fetching cart for user: {}", userId);
        
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("No cart found for user: {}, returning empty cart", userId);
                    return Cart.builder().userId(userId).build();
                });
        
        return CartMapper.toCartResponse(cart);
    }
}

