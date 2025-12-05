package com.blibli.gdn.cartService.service;

import com.blibli.gdn.cartService.model.Cart;
import com.blibli.gdn.cartService.model.CartItem;
import com.blibli.gdn.cartService.web.model.AddToCartRequest;
import com.blibli.gdn.cartService.web.model.UpdateQuantityRequest;

public interface CartService {
    Cart addToCart(String memberId, AddToCartRequest request);

    Cart getCart(String memberId);

    Cart updateQuantity(String memberId, String sku, UpdateQuantityRequest request);

    void removeItem(String memberId, String sku);

    void clearCart(String memberId);

    void mergeCarts(String guestCartId, String memberId);
}
