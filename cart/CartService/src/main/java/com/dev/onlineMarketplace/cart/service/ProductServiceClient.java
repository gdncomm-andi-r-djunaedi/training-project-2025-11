package com.dev.onlineMarketplace.cart.service;

import com.dev.onlineMarketplace.cart.client.ProductServiceFeignClient;
import com.dev.onlineMarketplace.cart.dto.GdnResponseData;
import com.dev.onlineMarketplace.cart.dto.Product;
import com.dev.onlineMarketplace.cart.exception.ProductNotFoundException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductServiceClient {

    private final ProductServiceFeignClient productServiceFeignClient;

    public Product getProductById(String productId) {
        log.info("Fetching product details for productId: {} from product service", productId);
        
        try {
            GdnResponseData<Product> response = productServiceFeignClient.getProductById(productId);

            if (response != null && response.isSuccess() && response.getData() != null) {
                Product product = response.getData();
                log.info("Successfully fetched product: {}", product.getName());
                return product;
            } else {
                log.error("Product not found or invalid response for productId: {}", productId);
                throw new ProductNotFoundException("Product not found with id: " + productId);
            }
        } catch (FeignException.NotFound e) {
            log.error("Product not found with id: {}", productId);
            throw new ProductNotFoundException("Product not found with id: " + productId);
        } catch (FeignException e) {
            log.error("Error calling product service for productId: {}, status: {}", productId, e.status());
            throw new RuntimeException("Error fetching product details: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error fetching product for productId: {}", productId, e);
            throw new RuntimeException("Unexpected error fetching product details: " + e.getMessage());
        }
    }
}

