package com.blibi.cart.feign;

import com.blibi.cart.dto.ProductResponseWrapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign Client interface for communicating with Product Service.
 * 
 * This interface enables declarative REST client functionality using Spring Cloud OpenFeign.
 * Feign automatically handles HTTP communication, serialization, and error handling.
 * 
 * Key Features:
 * - Declarative REST client (no implementation needed)
 * - Automatic HTTP request/response handling
 * - Integrates with Spring's dependency injection
 * - Handles service-to-service communication
 * 
 * @FeignClient: Marks this interface as a Feign client
 *   - name: Logical name of the service
 *   - url: Base URL of the Product Service
 * 
 * Usage:
 * This client is used in CartServiceImpl to fetch product details
 * when adding items to cart, ensuring we have the latest product information.
 * 
 * @author Cart Service Team
 * @version 1.0
 */
@FeignClient(name = "product", url = "http://localhost:8081")
public interface ProductFeignClient {

    /**
     * Fetches product details from Product Service by product ID.
     * 
     * This method makes an HTTP GET request to the Product Service
     * to retrieve product information including name, price, and other details.
     * 
     * Implementation Details:
     * - Feign automatically creates HTTP GET request
     * - Path variable {id} is replaced with productId parameter
     * - Response is automatically deserialized to ProductResponseWrapper
     * - Throws FeignException if service is unavailable or product not found
     * 
     * @param productId Product identifier to fetch
     * @return ProductResponseWrapper containing product data
     * @throws feign.FeignException if Product Service is unavailable or product not found
     * 
     * HTTP Method: GET
     * Endpoint: http://localhost:8080/api/product/{id}
     */
    @GetMapping("/api/product/{id}")
    ProductResponseWrapper getProduct(@PathVariable("id") String productId);
}
