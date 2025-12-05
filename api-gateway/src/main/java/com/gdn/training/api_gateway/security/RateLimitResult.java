package com.gdn.training.api_gateway.security;

public record RateLimitResult(boolean allowed, long remainingTokens, long nanosToReset) {

    public static RateLimitResult allowed(long remainingTokens, long nanosToReset) {
        return new RateLimitResult(true, Math.max(remainingTokens, 0), Math.max(nanosToReset, 0));
    }

    public static RateLimitResult allowed(long remainingTokens) {
        return allowed(remainingTokens, 0);
    }

    public static RateLimitResult blocked(long nanosToReset) {
        return new RateLimitResult(false, 0, Math.max(nanosToReset, 0));
    }
}

