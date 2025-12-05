package org.edmund.apigateway.properties;

import lombok.Data;

import java.util.List;

@Data
public class CorsMappingProperties {
  private String pathPattern;
  private List<String> allowedOrigins;
  private List<String> allowedMethods;
  private List<String> allowedHeaders;
  private List<String> exposedHeaders;
  private Boolean allowCredentials;
  private Long maxAge;
}
