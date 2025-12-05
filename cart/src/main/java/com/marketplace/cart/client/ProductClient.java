package com.marketplace.cart.client;

import com.marketplace.cart.dto.ProductDTO;
import com.marketplace.common.dto.ApiResponse;
import com.marketplace.common.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class ProductClient {

    private final RestTemplate restTemplate;
    private final String productServiceUrl;

    public ProductClient(
            RestTemplate restTemplate,
            @Value("${product-service.url:http://localhost:8082}") String productServiceUrl) {
        this.restTemplate = restTemplate;
        this.productServiceUrl = productServiceUrl;
    }

    /**
     * Get product by ID from product-service
     */
    public Optional<ProductDTO> getProductById(String productId) {
        try {
            String url = productServiceUrl + "/api/products/" + productId;
            log.debug("Fetching product from: {}", url);

            ResponseEntity<ApiResponse<ProductDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<ProductDTO>>() {}
            );

            if (response.getBody() != null && response.getBody().getData() != null) {
                return Optional.of(response.getBody().getData());
            }
            return Optional.empty();
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Product not found: {}", productId);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error fetching product {}: {}", productId, e.getMessage());
            throw new RuntimeException("Failed to fetch product from product-service", e);
        }
    }

    /**
     * Get product by ID, throws exception if not found
     */
    public ProductDTO getProductByIdOrThrow(String productId) {
        return getProductById(productId)
                .orElseThrow(() -> ResourceNotFoundException.of("Product", productId));
    }

    /**
     * Check if product exists
     */
    public boolean productExists(String productId) {
        return getProductById(productId).isPresent();
    }

    /**
     * Get multiple products by IDs
     */
    public List<ProductDTO> getProductsByIds(List<String> productIds) {
        try {
            String url = productServiceUrl + "/api/products/batch";
            log.debug("Fetching products batch from: {}", url);

            ResponseEntity<ApiResponse<List<ProductDTO>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new org.springframework.http.HttpEntity<>(productIds),
                    new ParameterizedTypeReference<ApiResponse<List<ProductDTO>>>() {}
            );

            if (response.getBody() != null && response.getBody().getData() != null) {
                return response.getBody().getData();
            }
            return List.of();
        } catch (Exception e) {
            log.error("Error fetching products batch: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch products from product-service", e);
        }
    }
}

