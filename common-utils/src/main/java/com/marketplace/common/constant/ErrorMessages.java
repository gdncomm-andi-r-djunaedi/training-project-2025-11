package com.marketplace.common.constant;

/**
 * Common error message constants used across services.
 */
public final class ErrorMessages {

    private ErrorMessages() {
        // Prevent instantiation
    }

    // Resource errors
    public static final String NOT_FOUND = "Resource not found";
    public static final String ALREADY_EXISTS = "Resource already exists";

    // Validation errors
    public static final String VALIDATION_FAILED = "Validation failed";
    public static final String INVALID_REQUEST = "Invalid request";

    // Authentication/Authorization errors
    public static final String UNAUTHORIZED = "Unauthorized access";
    public static final String FORBIDDEN = "Access denied";
    public static final String INVALID_CREDENTIALS = "Invalid credentials";

    // General errors
    public static final String INTERNAL_ERROR = "Internal server error";
    public static final String SERVICE_UNAVAILABLE = "Service temporarily unavailable";

    // Member-specific errors
    public static final String EMAIL_ALREADY_EXISTS = "Email already exists";
    public static final String PHONE_ALREADY_EXISTS = "Phone number already exists";
    public static final String MEMBER_NOT_FOUND = "Member not found";

    // Product-specific errors
    public static final String PRODUCT_NOT_FOUND = "Product not found";

    // Cart-specific errors
    public static final String CART_NOT_FOUND = "Cart not found";
    public static final String CART_ITEM_NOT_FOUND = "Cart item not found";
}
