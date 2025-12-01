package com.example.gateway.properties;

import com.example.gateway.model.RouteDefinition;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "gateway")
public class GatewayProperties {
  private List<RouteDefinition> routes;
}