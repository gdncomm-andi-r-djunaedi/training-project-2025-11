package com.Cart.CartService.exception;

import org.springframework.http.HttpStatus;

public class ProductServiceExceptions extends RuntimeException{
    private final HttpStatus status;

    public ProductServiceExceptions(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
