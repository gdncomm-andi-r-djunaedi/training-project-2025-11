package com.gdn.training.apigateway.infrastructure.security;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtProviderFactory {
    private final Map<String, JwtProvider> providers;

    public JwtProviderFactory(List<JwtProvider> providers) {
        this.providers = providers.stream()
                .collect(Collectors.toMap(JwtProvider::providerId, provider -> provider));
    }

    public JwtProvider getProvider(String providerId) {
        return providers.get(providerId);
    }

    public JwtProvider defaultProvider() {
        return providers.get("hmac");
    }
}
