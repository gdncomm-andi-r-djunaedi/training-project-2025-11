package org.edmund.apigateway.config;

import lombok.RequiredArgsConstructor;
import org.edmund.apigateway.properties.CorsMappingProperties;
import org.edmund.apigateway.properties.GatewayCorsProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class CorsConfig implements WebMvcConfigurer {

  private final GatewayCorsProperties corsProps;

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    if (corsProps.getMappings() == null) {
      return;
    }

    for (CorsMappingProperties mapping : corsProps.getMappings()) {
      var reg = registry.addMapping(mapping.getPathPattern());

      if (mapping.getAllowedOrigins() != null && !mapping.getAllowedOrigins().isEmpty()) {
        reg.allowedOrigins(mapping.getAllowedOrigins().toArray(String[]::new));
      }

      if (mapping.getAllowedMethods() != null && !mapping.getAllowedMethods().isEmpty()) {
        reg.allowedMethods(mapping.getAllowedMethods().toArray(String[]::new));
      }

      if (mapping.getAllowedHeaders() != null && !mapping.getAllowedHeaders().isEmpty()) {
        reg.allowedHeaders(mapping.getAllowedHeaders().toArray(String[]::new));
      }

      if (mapping.getExposedHeaders() != null && !mapping.getExposedHeaders().isEmpty()) {
        reg.exposedHeaders(mapping.getExposedHeaders().toArray(String[]::new));
      }

      if (mapping.getAllowCredentials() != null) {
        reg.allowCredentials(mapping.getAllowCredentials());
      }

      if (mapping.getMaxAge() != null) {
        reg.maxAge(mapping.getMaxAge());
      }
    }
  }
}
