package com.marketplace.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "gateway")
public class RouteProperties {
    private List<Route> routes = new ArrayList<>();

    @Data
    public static class Route {
        private String id;
        private String path;
        private String uri;
        private String filters;
    }
}

