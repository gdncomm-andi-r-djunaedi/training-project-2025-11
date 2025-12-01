package com.example.gateway.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "gateway.cors")
public class GatewayCorsProperties {

  /**
   * List of CORS mappings, each with its own path pattern and settings.
   */
  private List<CorsMappingProperties> mappings;
}
