package org.edmund.apigateway.properties;

import lombok.Data;
import org.edmund.apigateway.dto.Router;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "gateway")
public class GatewayProperties {
  private List<Router> routesList;
}