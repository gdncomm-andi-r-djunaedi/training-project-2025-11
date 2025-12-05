package com.dev.onlineMarketplace.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for defining public (open) and secured endpoints
 */
@Configuration
@ConfigurationProperties(prefix = "gateway.security")
public class SecurityConfig {

    /**
     * List of endpoint patterns that don't require authentication
     * Supports exact matches and wildcards
     */
    private List<String> openEndpoints = new ArrayList<>();

    /**
     * List of endpoint patterns that require authentication
     * If empty, all endpoints not in openEndpoints require auth by default
     */
    private List<String> securedEndpoints = new ArrayList<>();

    public List<String> getOpenEndpoints() {
        return openEndpoints;
    }

    public void setOpenEndpoints(List<String> openEndpoints) {
        this.openEndpoints = openEndpoints;
    }

    public List<String> getSecuredEndpoints() {
        return securedEndpoints;
    }

    public void setSecuredEndpoints(List<String> securedEndpoints) {
        this.securedEndpoints = securedEndpoints;
    }
}

