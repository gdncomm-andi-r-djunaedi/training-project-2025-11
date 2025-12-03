package com.microservice.cart.client;

import com.microservice.cart.dto.ProductResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", url = "http://localhost:8004")
public interface ProductFeign {
    @GetMapping("/api/products/{productId}")
    ProductResponseDto getProduct(@PathVariable("productId") Long productId);
}
