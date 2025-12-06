package com.marketplace.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for API Gateway
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "gateway")
public class GatewayConfigProperties {

    private Routing routing = new Routing();
    private RateLimit rateLimit = new RateLimit();

    @Data
    public static class Routing {
        private int connectionTimeout = 30000;
        private int responseTimeout = 60000;
    }

    @Data
    public static class RateLimit {
        private boolean enabled = false;
        private int requestsPerMinute = 100;
    }
}
