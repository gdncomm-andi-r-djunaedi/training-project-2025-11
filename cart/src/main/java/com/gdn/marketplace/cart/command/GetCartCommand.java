package com.gdn.marketplace.cart.command;

import com.gdn.marketplace.cart.entity.Cart;
import com.gdn.marketplace.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GetCartCommand implements Command<Cart, String> {

    @Autowired
    private CartService cartService;

    @Override
    public Cart execute(String username) {
        return cartService.getCart(username);
    }
}
