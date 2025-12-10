package com.marketplace.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested resource is not found.
 * This is a generic exception that can be used across all services
 * for any type of resource (Product, Cart, User, etc.)
 */
public class ResourceNotFoundException extends BaseException {

    private static final String ERROR_CODE = "RESOURCE_NOT_FOUND";
    private static final String MESSAGE_TEMPLATE = "%s not found: %s";

    /**
     * Create a ResourceNotFoundException with resource type and identifier.
     *
     * @param resourceType The type of resource (e.g., "Product", "Cart", "User")
     * @param identifier   The identifier that was not found
     */
    public ResourceNotFoundException(String resourceType, String identifier) {
        super(String.format(MESSAGE_TEMPLATE, resourceType, identifier),
                HttpStatus.NOT_FOUND.value(),
                ERROR_CODE);
    }

    /**
     * Create a ResourceNotFoundException with a custom message.
     *
     * @param message Custom error message
     */
    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND.value(), ERROR_CODE);
    }
}

