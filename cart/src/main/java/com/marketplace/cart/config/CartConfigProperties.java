package com.marketplace.cart.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Cart Service
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "cart")
public class CartConfigProperties {

    private Database database = new Database();
    private Pagination pagination = new Pagination();

    @Data
    public static class Database {
        private int maxConnections = 10;
        private int connectionTimeout = 30000;
    }

    @Data
    public static class Pagination {
        private int defaultPageSize = 10;
        private int maxPageSize = 100;
    }
}
