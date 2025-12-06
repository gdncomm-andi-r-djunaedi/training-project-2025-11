package com.gdn.training.apigateway.infrastructure.security;

public interface TokenBlacklistRepository {
    void blacklist(String jti);

    boolean isBlacklisted(String jti);
}
