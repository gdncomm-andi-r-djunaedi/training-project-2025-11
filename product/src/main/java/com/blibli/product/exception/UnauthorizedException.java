package com.blibli.product.exception;

/**
 * Exception thrown when user doesn't have permission to perform an operation
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}

