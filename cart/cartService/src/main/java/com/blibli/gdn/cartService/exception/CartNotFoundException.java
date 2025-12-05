package com.blibli.gdn.cartService.exception;

import org.springframework.http.HttpStatus;

public class CartNotFoundException extends CartException {
    public CartNotFoundException(String memberId) {
        super("Cart not found for member: " + memberId, HttpStatus.NOT_FOUND);
    }
}
