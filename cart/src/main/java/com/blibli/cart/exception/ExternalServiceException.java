package com.blibli.cart.exception;

public class ExternalServiceException extends RuntimeException {
    public ExternalServiceException(String messages) {
        super(messages);
    }

    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}

