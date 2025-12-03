package com.gdn.training.api_gateway.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimiterProperties {

    private boolean enabled = true;

    private long requestsPerMinute = 60;

    /**
     * Optional set of paths that are exempt from rate limiting checks. Supports Ant-style patterns.
     */
    private List<String> ignoredPaths = new ArrayList<>();
}

