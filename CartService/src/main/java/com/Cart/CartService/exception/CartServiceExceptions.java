package com.Cart.CartService.exception;

import org.springframework.http.HttpStatus;

public class CartServiceExceptions extends RuntimeException{
    public CartServiceExceptions(String message, HttpStatus status) {
        super(message);
    }
}

