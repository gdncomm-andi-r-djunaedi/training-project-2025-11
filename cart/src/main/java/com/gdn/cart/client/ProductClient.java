package com.gdn.cart.client;

import com.gdn.cart.client.model.ProductResponse;
import com.gdn.cart.exception.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductClient {

  private final RestTemplate restTemplate;

  @Value("${product.service.url}")
  private String productServiceUrl;

  public ProductResponse getProductById(String productId) {
    String url = productServiceUrl + "/products/" + productId;
    log.info("Fetching product from: {}", url);

    try {
      ProductResponse product = restTemplate.getForObject(url, ProductResponse.class);
      
      if (product == null) {
        throw new DataNotFoundException();
      }
      
      log.info("Product found: {} - {}", product.getId(), product.getName());
      return product;
      
    } catch (HttpClientErrorException.NotFound e) {
      log.error("Product not found: {}", productId);
      throw new DataNotFoundException();
    } catch (Exception e) {
      log.error("Error fetching product: {}", e.getMessage());
      throw new RuntimeException("Failed to fetch product details: " + e.getMessage());
    }
  }
}

