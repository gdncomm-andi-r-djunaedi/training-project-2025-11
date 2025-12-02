package com.gdn.training.cart.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "services")
public class ServiceClientsProperties {

    private Map<String, ServiceClientConfig> clients = new HashMap<>();

    public ServiceClientConfig getRequired(String name) {
        ServiceClientConfig config = clients.get(name);
        if (config == null) {
            throw new IllegalArgumentException("Unknown service: " + name);
        }
        return config;
    }
}
