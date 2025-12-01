package com.example.gateway.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "gateway.jwt")
public class JwtProperties {

  private String secret;
  private long ttlSeconds = 3600; // optional
}
