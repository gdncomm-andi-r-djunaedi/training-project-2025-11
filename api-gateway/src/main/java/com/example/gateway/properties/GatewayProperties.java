package com.example.gateway.properties;

import com.example.gateway.routing.RouteDefinition;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "gateway")
public class GatewayProperties {
  private List<RouteDefinition> routes;
}