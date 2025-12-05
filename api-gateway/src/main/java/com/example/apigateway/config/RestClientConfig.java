package com.example.apigateway.config;

import com.example.apigateway.webclient.UserIdForwardingInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;

@Configuration
public class RestClientConfig {

  @Bean
  public RestTemplate restTemplate(UserIdForwardingInterceptor interceptor) {
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.setInterceptors(Collections.singletonList(interceptor));
    restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
      @Override
      public void handleError(ClientHttpResponse response) throws IOException {
        if (response.getStatusCode().is4xxClientError() ||
                response.getStatusCode().is5xxServerError()) {
          throw new HttpClientErrorException(
                  response.getStatusCode(),
                  response.getStatusText(),
                  response.getHeaders(),
                  StreamUtils.copyToByteArray(response.getBody()),
                  null
          );
        }
      }
    });
    return restTemplate;
  }
}