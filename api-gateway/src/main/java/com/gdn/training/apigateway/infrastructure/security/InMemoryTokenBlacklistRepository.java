package com.gdn.training.apigateway.infrastructure.security;

import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryTokenBlacklistRepository implements TokenBlacklistRepository {
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    @Override
    public void blacklist(String jti) {
        if (jti != null) {
            blacklistedTokens.add(jti);
        }
    }

    @Override
    public boolean isBlacklisted(String jti) {
        return jti != null && blacklistedTokens.contains(jti);
    }
}
