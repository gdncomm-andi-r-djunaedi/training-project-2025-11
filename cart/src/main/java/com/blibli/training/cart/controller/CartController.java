package com.blibli.training.cart.controller;

import com.blibli.training.cart.entity.Cart;
import com.blibli.training.cart.entity.CartItem;
import com.blibli.training.cart.repository.CartRepository;
import com.blibli.training.framework.dto.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartRepository cartRepository;

    @GetMapping
    public BaseResponse<Cart> getCart(@RequestHeader("X-User-Id") Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(Cart.builder().userId(userId).build()));
        return BaseResponse.success(cart);
    }

    @PostMapping("/items")
    public BaseResponse<Cart> addItem(@RequestHeader("X-User-Id") Long userId, @RequestBody CartItem item) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(Cart.builder().userId(userId).build()));

        item.setCart(cart);
        cart.getItems().add(item);

        return BaseResponse.success(cartRepository.save(cart));
    }
}
