package com.kailash.cart.client;

import com.kailash.cart.dto.ApiResponse;
import com.kailash.cart.dto.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product",url = "http://localhost:8086")
public interface ProductClient {

    @GetMapping("/products/{sku}")
    public ResponseEntity<ApiResponse<ProductResponse>> get(@PathVariable("sku") String sku);
}
