package org.edmund.apigateway.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "gateway.cors")
public class GatewayCorsProperties {
  private List<CorsMappingProperties> mappings;
}
