package com.example.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Configuration
public class RestClientConfig {

  @Bean
  public RestTemplate restTemplate() {
    RestTemplate rt = new RestTemplate();
    // Do NOT throw on 4xx/5xx; let us proxy the status & body
    rt.setErrorHandler(new ResponseErrorHandler() {
      @Override
      public boolean hasError(ClientHttpResponse response) throws IOException {
        return false;
      }

      @Override
      public void handleError(ClientHttpResponse response) throws IOException {
        // no-op
      }
    });
    return rt;
  }
}
