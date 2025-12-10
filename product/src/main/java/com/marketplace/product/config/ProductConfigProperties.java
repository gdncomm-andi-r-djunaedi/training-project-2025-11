package com.marketplace.product.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Product Service
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "product")
public class ProductConfigProperties {

    private Search search = new Search();
    private Cache cache = new Cache();

    @Data
    public static class Search {
        private int defaultPageSize = 20;
        private int maxPageSize = 100;
        private boolean enableFuzzySearch = true;
    }

    @Data
    public static class Cache {
        private boolean enabled = true;
        private int ttlMinutes = 30;
    }
}
