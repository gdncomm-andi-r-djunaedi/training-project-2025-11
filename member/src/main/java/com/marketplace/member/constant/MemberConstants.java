package com.marketplace.member.constant;

/**
 * Constants for Member Service
 */
public final class MemberConstants {

    private MemberConstants() {
        // Prevent instantiation
    }

    /**
     * Error messages
     */
    public static final class ErrorMessages {
        public static final String USER_NOT_FOUND = "User not found: %s";
        public static final String USERNAME_EXISTS = "Username already exists: %s";
        public static final String EMAIL_EXISTS = "Email already exists: %s";
        public static final String INVALID_CREDENTIALS = "Invalid username or password";

        private ErrorMessages() {
        }
    }

    /**
     * Validation messages
     */
    public static final class ValidationMessages {
        public static final String USERNAME_REQUIRED = "Username is required";
        public static final String USERNAME_SIZE = "Username must be between 3 and 50 characters";
        public static final String PASSWORD_REQUIRED = "Password is required";
        public static final String PASSWORD_SIZE = "Password must be at least 8 characters";
        public static final String EMAIL_REQUIRED = "Email is required";
        public static final String EMAIL_INVALID = "Email must be a valid email address";
        public static final String FULL_NAME_REQUIRED = "Full name is required";

        private ValidationMessages() {
        }
    }

    /**
     * JWT related constants (in addition to common constants)
     */
    public static final class Jwt {
        public static final long DEFAULT_EXPIRATION = 86400000L; // 24 hours

        private Jwt() {
        }
    }
}
