package com.blibli.gdn.cartService.exception;

import org.springframework.http.HttpStatus;

public class ProductServiceUnavailableException extends CartException {
    public ProductServiceUnavailableException(String message) {
        super("Product Service unavailable: " + message, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
