package org.jovian.cart.service;

import java.util.List;
import org.jovian.cart.entity.Cart;
import org.jovian.cart.entity.CartItem;

public interface CartService {
    void addProduct(Long cartId, String productId, int quantity);

    void removeProduct(Long cartId, String productId, int quantity);

    List<CartItem> getCartProducts(Long cartId);

    Cart getOrCreateCart(Long cartId);

    void clearCart(Long cartId);
}
