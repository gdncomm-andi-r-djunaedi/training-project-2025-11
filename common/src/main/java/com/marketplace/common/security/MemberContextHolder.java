package com.marketplace.common.security;

/**
 * Thread-local holder for the current authenticated member context.
 * Used by downstream services to access member information from JWT.
 */
public class MemberContextHolder {

    private static final ThreadLocal<MemberContext> contextHolder = new ThreadLocal<>();

    public static void setContext(MemberContext context) {
        contextHolder.set(context);
    }

    public static MemberContext getContext() {
        return contextHolder.get();
    }

    public static void clearContext() {
        contextHolder.remove();
    }

    public static boolean hasContext() {
        return contextHolder.get() != null;
    }
}

