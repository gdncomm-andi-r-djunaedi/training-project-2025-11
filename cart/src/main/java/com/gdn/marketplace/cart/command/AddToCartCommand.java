package com.gdn.marketplace.cart.command;

import com.gdn.marketplace.cart.entity.Cart;
import com.gdn.marketplace.cart.entity.CartItem;
import com.gdn.marketplace.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AddToCartCommand implements Command<Cart, AddToCartCommand.Request> {

    @Autowired
    private CartService cartService;

    @Override
    public Cart execute(Request request) {
        return cartService.addToCart(request.getUsername(), request.getItem());
    }

    public static class Request {
        private String username;
        private CartItem item;

        public Request(String username, CartItem item) {
            this.username = username;
            this.item = item;
        }

        public String getUsername() {
            return username;
        }

        public CartItem getItem() {
            return item;
        }
    }
}
