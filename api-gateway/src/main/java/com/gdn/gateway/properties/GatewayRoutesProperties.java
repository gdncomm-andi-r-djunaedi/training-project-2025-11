package com.gdn.gateway.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "gateway")
public class GatewayRoutesProperties {
    private List<RouteConfig> routes;

    @Data
    public static class RouteConfig {
        private String name;
        private String path;
        private String uri;
    }
}
