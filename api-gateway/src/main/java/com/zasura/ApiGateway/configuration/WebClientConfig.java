package com.zasura.apiGateway.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Slf4j
public class WebClientConfig {

  @Value("${member.service.base-url}")
  private String memberServiceBaseUrl;

  @Bean
  public WebClient memberServiceWebClient() {
    return WebClient.builder().baseUrl(memberServiceBaseUrl).build();
  }
}
