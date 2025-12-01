package com.example.cart.service.impl;

import com.example.cart.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@Slf4j
public class ProductServiceImpl implements ProductService {

  private final RestClient restClient;

  public ProductServiceImpl(@Value("${product-service.base-url:http://localhost:8082}") String productBaseUrl) {
    this.restClient = RestClient.builder().baseUrl(productBaseUrl).build();
  }

  @Override
  public boolean exists(String productId) {
    try {
      // HEAD is ideal, but since we have only GET /api/products/{id},
      // we can call it and ignore the body.
      var response = restClient.get().uri("/api/products/{id}", productId).retrieve().toBodilessEntity();
      return response.getStatusCode().is2xxSuccessful();
    } catch (RestClientException ex) {
      log.warn("Failed to verify product {} existence: {}", productId, ex.getMessage());
      return false;
    }
  }
}
