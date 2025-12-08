package com.microservice.cart.client;

import com.microservice.cart.dto.ProductResponseDto;
//import com.microservice.product.dto.ProductResponseDto;
import com.microservice.cart.wrapper.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "product-service", url = "http://localhost:8004")
public interface ProductFeign {
    @GetMapping("/api/products/{productId}")
    ApiResponse<ProductResponseDto> getProduct(@PathVariable("productId") String productId);

    @PostMapping("/api/products/getSkusById")
    ApiResponse<List<ProductResponseDto>> getProductsBySkuIds(@RequestBody List<String> skuIds);
}