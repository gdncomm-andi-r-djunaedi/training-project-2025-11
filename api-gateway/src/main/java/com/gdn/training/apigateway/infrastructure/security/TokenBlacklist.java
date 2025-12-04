package com.gdn.training.apigateway.infrastructure.security;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class TokenBlacklist {

    private final Set<String> blacklist = ConcurrentHashMap.newKeySet();

    public void blacklist(String token) {
        blacklist.add(token);
    }

    public boolean isBlacklisted(String token) {
        return blacklist.contains(token);
    }
}