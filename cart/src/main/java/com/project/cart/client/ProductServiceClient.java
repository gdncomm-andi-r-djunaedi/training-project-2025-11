package com.project.cart.client;

import com.project.cart.dto.ProductDto;
import com.project.cart.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

/**
 * Client for communicating with Product Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${product-service.base-url}")
    private String productServiceBaseUrl;

    @Value("${product-service.timeout}")
    private int timeout;

    /**
     * Get product by ID from Product Service
     */
    public ProductDto getProductById(String productId) {
        log.info("Fetching product from Product Service: {}", productId);

        try {
            return webClientBuilder.build()
                    .get()
                    .uri(productServiceBaseUrl + "/v1/products/{id}", productId)
                    .retrieve()
                    .bodyToMono(ProductDto.class)
                    .timeout(Duration.ofMillis(timeout))
                    .block();
        } catch (Exception e) {
            log.error("Error fetching product {}: {}", productId, e.getMessage());
            throw new ProductNotFoundException("Product not found: " + productId);
        }
    }

    /**
     * Get multiple products by IDs (batch)
     */
    public List<ProductDto> getProductsByIds(List<String> productIds) {
        log.info("Fetching {} products from Product Service", productIds.size());

        try {
            return webClientBuilder.build()
                    .post()
                    .uri(productServiceBaseUrl + "/v1/products/batch")
                    .bodyValue(productIds)
                    .retrieve()
                    .bodyToFlux(ProductDto.class)
                    .timeout(Duration.ofMillis(timeout))
                    .collectList()
                    .block();
        } catch (Exception e) {
            log.error("Error fetching products: {}", e.getMessage());
            throw new ProductNotFoundException("Error fetching products");
        }
    }

    /**
     * Validate product exists and is active
     */
    public boolean isProductValid(String productId) {
        try {
            ProductDto product = getProductById(productId);
            return product != null && Boolean.TRUE.equals(product.getIsActive());
        } catch (Exception e) {
            return false;
        }
    }
}