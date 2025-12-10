package com.marketplace.common.constant;

/**
 * Common error message constants used across services.
 * Use these constants for consistent error messaging.
 */
public final class ErrorMessages {

    private ErrorMessages() {
        // Prevent instantiation
    }

    /**
     * Resource-related error messages
     */
    public static final class Resource {
        public static final String NOT_FOUND = "%s not found: %s";
        public static final String ALREADY_EXISTS = "%s already exists: %s";

        private Resource() {
        }
    }

    /**
     * Validation error messages
     */
    public static final class Validation {
        public static final String FAILED = "Validation failed";
        public static final String INVALID_REQUEST = "Invalid request";
        public static final String REQUIRED_FIELD = "%s is required";
        public static final String INVALID_FORMAT = "Invalid %s format";

        private Validation() {
        }
    }

    /**
     * Authentication/Authorization error messages
     */
    public static final class Auth {
        public static final String UNAUTHORIZED = "Unauthorized access";
        public static final String FORBIDDEN = "Access denied";
        public static final String INVALID_CREDENTIALS = "Invalid credentials";
        public static final String TOKEN_EXPIRED = "Token has expired";
        public static final String TOKEN_INVALID = "Invalid token";

        private Auth() {
        }
    }

    /**
     * System error messages
     */
    public static final class System {
        public static final String INTERNAL_ERROR = "An unexpected error occurred";
        public static final String SERVICE_UNAVAILABLE = "Service temporarily unavailable";
        public static final String DATABASE_ERROR = "Database operation failed";

        private System() {
        }
    }

    // Legacy constants for backward compatibility
    public static final String NOT_FOUND = "Resource not found";
    public static final String ALREADY_EXISTS = "Resource already exists";
    public static final String VALIDATION_FAILED = "Validation failed";
    public static final String INVALID_REQUEST = "Invalid request";
    public static final String UNAUTHORIZED = "Unauthorized access";
    public static final String FORBIDDEN = "Access denied";
    public static final String INVALID_CREDENTIALS = "Invalid credentials";
    public static final String INTERNAL_ERROR = "Internal server error";
    public static final String SERVICE_UNAVAILABLE = "Service temporarily unavailable";
    public static final String EMAIL_ALREADY_EXISTS = "Email already exists";
    public static final String PHONE_ALREADY_EXISTS = "Phone number already exists";
    public static final String MEMBER_NOT_FOUND = "Member not found";
    public static final String PRODUCT_NOT_FOUND = "Product not found";
    public static final String CART_NOT_FOUND = "Cart not found";
    public static final String CART_ITEM_NOT_FOUND = "Cart item not found";
}
