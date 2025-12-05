package com.example.member.security;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryTokenBlacklist {
    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();

    public void blacklist(String token) {
        blacklist.put(token, System.currentTimeMillis());
    }

    public boolean isBlacklisted(String token) {
        return blacklist.containsKey(token);
    }
}
