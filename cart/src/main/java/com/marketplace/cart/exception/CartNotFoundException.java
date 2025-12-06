package com.marketplace.cart.exception;

import com.marketplace.cart.constant.CartConstants;
import com.marketplace.common.exception.BaseException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a cart is not found for a user
 */
public class CartNotFoundException extends BaseException {

    public CartNotFoundException(String username) {
        super(String.format(CartConstants.ErrorMessages.CART_NOT_FOUND, username),
                HttpStatus.NOT_FOUND.value(),
                "CART_NOT_FOUND");
    }
}
