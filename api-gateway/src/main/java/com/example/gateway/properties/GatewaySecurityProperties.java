package com.example.gateway.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "gateway.security")
public class GatewaySecurityProperties {

  /**
   * Paths that are always public (permitAll).
   * Example: /api/members/register, /api/members/login, /swagger-ui/**, etc.
   */
  private List<String> publicPaths;

  /**
   * Paths that require authentication.
   * Example: /api/cart/**, /api/members/logout, etc.
   */
  private List<String> authenticatedPaths;
}