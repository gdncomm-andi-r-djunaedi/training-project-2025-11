package com.gdn.marketplace.cart.command;

import com.gdn.marketplace.cart.entity.Cart;
import com.gdn.marketplace.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RemoveFromCartCommand implements Command<Cart, RemoveFromCartCommand.Request> {

    @Autowired
    private CartService cartService;

    @Override
    public Cart execute(Request request) {
        return cartService.removeFromCart(request.getUsername(), request.getProductId());
    }

    public static class Request {
        private String username;
        private String productId;

        public Request(String username, String productId) {
            this.username = username;
            this.productId = productId;
        }

        public String getUsername() {
            return username;
        }

        public String getProductId() {
            return productId;
        }
    }
}
