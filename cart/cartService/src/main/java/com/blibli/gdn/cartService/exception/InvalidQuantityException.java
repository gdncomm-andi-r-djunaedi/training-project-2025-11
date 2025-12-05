package com.blibli.gdn.cartService.exception;

import org.springframework.http.HttpStatus;

public class InvalidQuantityException extends CartException {
    public InvalidQuantityException(Integer qty) {
        super("Quantity must be a positive integer. Provided: " + qty, HttpStatus.BAD_REQUEST);
    }
}
