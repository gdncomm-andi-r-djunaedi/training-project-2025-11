package com.gdn.training.apigateway.infrastructure.security;

import com.gdn.training.apigateway.application.port.BlacklistPort;
import org.springframework.stereotype.Component;

/**
 * Adapter that delegates application-port BlacklistPort to infra
 * InMemoryTokenBlacklistRepository.
 */
@Component
public class InMemoryBlacklistAdapter implements BlacklistPort {

    private final InMemoryTokenBlacklistRepository inMemory;

    public InMemoryBlacklistAdapter(InMemoryTokenBlacklistRepository inMemory) {
        this.inMemory = inMemory;
    }

    @Override
    public void blacklist(String jti) {
        inMemory.blacklist(jti);
    }

    @Override
    public boolean isBlacklisted(String jti) {
        return inMemory.isBlacklisted(jti);
    }
}
