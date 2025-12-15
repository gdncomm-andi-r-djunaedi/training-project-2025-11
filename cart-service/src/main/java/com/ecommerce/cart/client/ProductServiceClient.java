package com.ecommerce.cart.client;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ProductServiceClient {

    private final RestTemplate restTemplate;

    @Value("${product.service.url}")
    private String productServiceUrl;

    public ProductServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean productExists(String productId) {
        try {
            restTemplate.getForObject(productServiceUrl + "/api/products/" + productId, ProductDto.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Data
    private static class ProductDto {
        private String id;
        private String name;
        private Integer stock;
    }
}
