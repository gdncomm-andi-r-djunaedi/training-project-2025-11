package com.marketplace.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secretKey = "secret-key";
    private long expirationSeconds = 3600;
    private String issuer = "marketplace-api";
    private String cookieName = "auth-token";
    private String headerName = "Authorization";
    private String headerPrefix = "Bearer ";
}

