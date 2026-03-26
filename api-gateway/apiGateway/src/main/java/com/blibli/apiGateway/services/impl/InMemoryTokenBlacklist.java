package com.blibli.apiGateway.services.impl;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryTokenBlacklist {

    private final Map<String, Long> blacklistedTokens = new ConcurrentHashMap<>();

    public void blacklistToken(String token, long expiryTimestamp) {
        blacklistedTokens.put(token, expiryTimestamp);
    }

    public boolean isTokenBlacklisted(String token) {
        Long expiry = blacklistedTokens.get(token);
        if (expiry == null) return false;

        if (System.currentTimeMillis() > expiry) {
            blacklistedTokens.remove(token);
            return false;
        }
        return true;
    }
}
