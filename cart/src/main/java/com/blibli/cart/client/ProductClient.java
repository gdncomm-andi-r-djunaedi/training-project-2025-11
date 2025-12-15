package com.blibli.cart.client;

import com.blibli.cart.dto.ApiResponse;
import com.blibli.cart.dto.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "product-service", url = "${product.service.url}")
public interface ProductClient {

    @GetMapping("/api/products/id/{productId}")
//    ProductResponse getProductById(@PathVariable("productId") String productId);

    ApiResponse<ProductResponse> getProductById(@PathVariable("productId") String productId);

    @GetMapping("/api/products/sku/{sku}")
    ProductResponse getProductBySku(@PathVariable("sku") String sku);
//
//    @PostMapping("/api/products/batch")
//    List<ProductResponse> getProductsByIds(@RequestBody List<String> productIds);
}

