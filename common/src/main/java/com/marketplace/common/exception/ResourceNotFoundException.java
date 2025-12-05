package com.marketplace.common.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BaseException {
    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "NOT_FOUND");
    }

    public static ResourceNotFoundException of(String resource, Object id) {
        return new ResourceNotFoundException(String.format("%s with id '%s' not found", resource, id));
    }
}

