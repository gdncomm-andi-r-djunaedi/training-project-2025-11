package com.marketplace.cart.client;

import com.marketplace.cart.dto.response.ProductDetailsResponse;
import com.marketplace.common.dto.ApiResponse;
import com.marketplace.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP client for communicating with Product Service.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.product.url:http://localhost:8082}")
    private String productServiceUrl;

    /**
     * Fetch product details by ID from Product Service.
     *
     * @param productId The product ID
     * @return Product details
     * @throws ResourceNotFoundException if product is not found
     */
    @SuppressWarnings("unchecked")
    public ProductDetailsResponse getProductById(String productId) {
        log.info("Fetching product details for ID: {}", productId);

        try {
            String url = productServiceUrl + "/api/product/" + productId;
            var response = restTemplate.getForObject(url, ApiResponse.class);

            if (response == null || response.getData() == null) {
                log.error("Product not found: {}", productId);
                throw new ResourceNotFoundException("Product", productId);
            }

            // Convert LinkedHashMap to ProductDetailsResponse
            Object data = response.getData();
            if (data instanceof java.util.Map) {
                java.util.Map<String, Object> map = (java.util.Map<String, Object>) data;
                return ProductDetailsResponse.builder()
                        .id((String) map.get("id"))
                        .name((String) map.get("name"))
                        .price(new java.math.BigDecimal(map.get("price").toString()))
                        .build();
            }

            throw new ResourceNotFoundException("Product", productId);

        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            log.error("Product not found: {}", productId);
            throw new ResourceNotFoundException("Product", productId);
        } catch (Exception e) {
            log.error("Error fetching product {}: {}", productId, e.getMessage());
            throw new RuntimeException("Failed to fetch product details", e);
        }
    }
}
