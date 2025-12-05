package com.marketplace.common.constants;

public final class AppConstants {
    private AppConstants() {}

    // Pagination
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;

    // JWT
    public static final String JWT_HEADER = "Authorization";
    public static final String JWT_PREFIX = "Bearer ";

    // Redis Keys
    public static final String CART_CACHE_PREFIX = "cart:";
    public static final String MEMBER_CACHE_PREFIX = "member:";
    public static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";

    // Headers for internal communication
    public static final String MEMBER_ID_HEADER = "X-Member-Id";
    public static final String MEMBER_EMAIL_HEADER = "X-Member-Email";
}

