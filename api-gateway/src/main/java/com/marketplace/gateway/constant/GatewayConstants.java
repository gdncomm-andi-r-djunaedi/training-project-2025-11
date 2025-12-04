package com.marketplace.gateway.constant;

/**
 * Constants for API Gateway.
 */
public final class GatewayConstants {

    private GatewayConstants() {
        // Prevent instantiation
    }

    /**
     * Error messages.
     */
    public static final class ErrorMessages {
        public static final String INVALID_TOKEN = "Invalid or expired JWT token";
        public static final String MISSING_TOKEN = "JWT token is missing";
        public static final String EXPIRED_TOKEN = "JWT token has expired";
        public static final String MALFORMED_TOKEN = "JWT token is malformed";
        public static final String SERVICE_UNAVAILABLE = "Service temporarily unavailable";

        private ErrorMessages() {
        }
    }

    /**
     * Validation messages.
     */
    public static final class ValidationMessages {
        public static final String EMAIL_REQUIRED = "Email is required";
        public static final String EMAIL_INVALID = "Invalid email format";
        public static final String PASSWORD_REQUIRED = "Password is required";

        private ValidationMessages() {
        }
    }

    /**
     * Route paths.
     */
    public static final class Routes {
        public static final String MEMBER_SERVICE = "lb://member-service";
        public static final String PRODUCT_SERVICE = "lb://product-service";
        public static final String CART_SERVICE = "lb://cart-service";

        private Routes() {
        }
    }

    /**
     * Headers passed to downstream services.
     */
    public static final class Headers {
        public static final String USER_EMAIL = "X-User-Email";
        public static final String USER_ID = "X-User-Id";

        private Headers() {
        }
    }
}
