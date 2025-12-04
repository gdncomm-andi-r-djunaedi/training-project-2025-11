package com.blibi.blibligatway.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


@Slf4j
@Service
public class TokenBlacklistService {

    private final Cache<String, Boolean> blacklist;

    public TokenBlacklistService() {
        // Cache tokens for up to 7 days (max token lifetime)
        // Individual entries expire based on their TTL when added
        this.blacklist = Caffeine.newBuilder()
                .maximumSize(100_000) // Maximum 100k blacklisted tokens
                .expireAfterWrite(7, TimeUnit.DAYS) // Safety expiration
                .build();
    }

    public void blacklistToken(String token, long expirationTimeMillis) {
        if (token == null || token.isEmpty()) {
            return;
        }
        
        // Calculate TTL: time until token expires
        long ttlMillis = Math.max(0, expirationTimeMillis - System.currentTimeMillis());
        
        if (ttlMillis > 0) {
            blacklist.put(token, true);
            log.debug("Token blacklisted. Will expire in {} seconds", ttlMillis / 1000);
        } else {
            log.debug("Token already expired, skipping blacklist");
        }
    }

    public boolean isBlacklisted(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        return blacklist.getIfPresent(token) != null;
    }

    public void removeFromBlacklist(String token) {
        if (token != null && !token.isEmpty()) {
            blacklist.invalidate(token);
            log.debug("Token removed from blacklist");
        }
    }
}

