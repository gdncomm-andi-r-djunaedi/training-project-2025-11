package com.blibli.cart.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String messages) {
        super(messages);
    }
}
