package com.example.gateway.properties;

import lombok.Data;

@Data
public class RateLimitRuleProperties {
  private String pathPattern;
  private long requests;
  private long windowSeconds;
}
