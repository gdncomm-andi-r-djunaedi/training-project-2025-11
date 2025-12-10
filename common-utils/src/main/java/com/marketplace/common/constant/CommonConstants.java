package com.marketplace.common.constant;

/**
 * Common constants shared across all microservices
 */
public final class CommonConstants {

    private CommonConstants() {
        // Prevent instantiation
    }

    /**
     * JWT related constants
     */
    public static final class Jwt {
        public static final String BEARER_PREFIX = "Bearer ";
        public static final String AUTHORIZATION_HEADER = "Authorization";
        public static final int BEARER_PREFIX_LENGTH = 7;

        private Jwt() {
        }
    }

    /**
     * HTTP related constants
     */
    public static final class Http {
        public static final String CONTENT_TYPE_JSON = "application/json";
        public static final String CHARSET_UTF8 = "UTF-8";

        private Http() {
        }
    }

    /**
     * Date/Time format constants
     */
    public static final class DateTime {
        public static final String ISO_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
        public static final String DATE_FORMAT = "yyyy-MM-dd";

        private DateTime() {
        }
    }

    /**
     * Error codes
     */
    public static final class ErrorCode {
        public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
        public static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
        public static final String UNAUTHORIZED = "UNAUTHORIZED";
        public static final String FORBIDDEN = "FORBIDDEN";
        public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
        public static final String BAD_REQUEST = "BAD_REQUEST";

        private ErrorCode() {
        }
    }
}
