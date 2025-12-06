package com.marketplace.cart.exception;

import com.marketplace.common.exception.BaseException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a product is not found in the cart
 */
public class CartItemNotFoundException extends BaseException {

    public CartItemNotFoundException(String productId) {
        super(String.format("Product '%s' not found in cart", productId),
                HttpStatus.NOT_FOUND.value(),
                "CART_ITEM_NOT_FOUND");
    }
}

