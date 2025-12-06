package com.marketplace.gateway.util;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Utility class for creating secure HTTP cookies
 */
@Component
public class CookieUtil {

    private static final String AUTH_COOKIE_NAME = "auth_token";
    private static final String SAME_SITE_STRICT = "Strict";

    /**
     * Create a secure authentication cookie
     */
    public ResponseCookie createAuthCookie(String token, long expirationMs) {
        return ResponseCookie.from(AUTH_COOKIE_NAME, token)
                .httpOnly(true) // Prevents JavaScript access
                .secure(true) // HTTPS only (set to false for local dev if needed)
                .sameSite(SAME_SITE_STRICT) // CSRF protection
                .maxAge(Duration.ofMillis(expirationMs))
                .path("/") // Available for all paths
                .build();
    }

    /**
     * Create a cookie to invalidate/logout
     */
    public ResponseCookie createLogoutCookie() {
        return ResponseCookie.from(AUTH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .sameSite(SAME_SITE_STRICT)
                .maxAge(Duration.ZERO) // Immediate expiration
                .path("/")
                .build();
    }

    /**
     * Get the cookie name used for authentication
     */
    public String getAuthCookieName() {
        return AUTH_COOKIE_NAME;
    }
}
