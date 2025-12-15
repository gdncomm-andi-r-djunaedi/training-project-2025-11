package org.edmund.cart.serviceimpl;

import lombok.extern.slf4j.Slf4j;
import org.edmund.cart.services.ProductService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@Slf4j
@Service
public class ProductServiceImpl implements ProductService {
    private final RestClient restClient;

    public ProductServiceImpl(@Value("${product-service.base-url:http://localhost:8082}") String productBaseUrl) {
        this.restClient = RestClient.builder().baseUrl(productBaseUrl).build();
    }

    @Override
    public boolean exists(String productSku) {
        try {
            System.out.println(restClient.get().uri("/api/products/search/product?sku={sku}", productSku));
            return restClient.get()
                    .uri("/api/products/search/product?sku={sku}", productSku)
                    .retrieve()
                    .toBodilessEntity() // gapake body, cuma peduli status
                    .getStatusCode()
                    .is2xxSuccessful();
        } catch (Exception ex) {
            log.warn("Product verification failed for SKU {}: {}", productSku, ex.getMessage());
            return false;
        }
    }
}
