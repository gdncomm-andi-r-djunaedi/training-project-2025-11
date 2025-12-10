package com.marketplace.common.exception;

import lombok.Getter;

/**
 * Base exception class for all custom exceptions in the marketplace platform.
 * Provides common fields and behavior for domain-specific exceptions.
 */
@Getter
public class BaseException extends RuntimeException {

    /**
     * HTTP status code to return when this exception is thrown
     */
    private final int statusCode;

    /**
     * Error code for client-side error handling
     */
    private final String errorCode;

    public BaseException(String message, int statusCode, String errorCode) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }

    public BaseException(String message, Throwable cause, int statusCode, String errorCode) {
        super(message, cause);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }
}
