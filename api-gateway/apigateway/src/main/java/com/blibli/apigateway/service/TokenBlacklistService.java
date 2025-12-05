package com.blibli.apigateway.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class TokenBlacklistService {

    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    public void blacklistToken(String token) {
        if (token != null && !token.trim().isEmpty()) {
            blacklistedTokens.add(token.trim());
            log.info("Token added to blacklist (blacklist size: {})", blacklistedTokens.size());
        }
    }

    public boolean isTokenBlacklisted(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        boolean isBlacklisted = blacklistedTokens.contains(token.trim());
        if (isBlacklisted) {
            log.warn("Token is blacklisted (attempted use after logout)");
        }
        return isBlacklisted;
    }

    public void removeFromBlacklist(String token) {
        if (token != null && !token.trim().isEmpty()) {
            blacklistedTokens.remove(token.trim());
            log.info("Token removed from blacklist (blacklist size: {})", blacklistedTokens.size());
        }
    }
    public int getBlacklistSize() {
        return blacklistedTokens.size();
    }
}

