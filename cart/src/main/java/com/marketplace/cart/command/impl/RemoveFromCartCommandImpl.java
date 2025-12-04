package com.marketplace.cart.command.impl;

import com.marketplace.cart.command.RemoveFromCartCommand;
import com.marketplace.cart.dto.request.RemoveFromCartRequest;
import com.marketplace.cart.dto.response.CartResponse;
import com.marketplace.cart.entity.Cart;
import com.marketplace.cart.exception.CartNotFoundException;
import com.marketplace.cart.mapper.CartMapper;
import com.marketplace.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveFromCartCommandImpl implements RemoveFromCartCommand {

    private final CartRepository cartRepository;

    @Override
    @Transactional
    public CartResponse execute(RemoveFromCartRequest request) {
        var userId = request.getUserId();
        var productId = request.getProductId();
        
        log.info("Removing product {} from cart for user: {}", productId, userId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + userId));

        int initialSize = cart.getItems().size();
        cart.getItems().removeIf(item -> item.getProductId().equals(productId));

        if (cart.getItems().size() == initialSize) {
            log.warn("Product {} not found in cart for user: {}", productId, userId);
        } else {
            log.info("Removed product {} from cart for user: {}", productId, userId);
        }

        Cart savedCart = cartRepository.save(cart);
        log.info("Cart updated successfully for user: {}", userId);
        
        return CartMapper.toCartResponse(savedCart);
    }
}

