package com.example.cartservice.client;

import com.example.cartservice.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", url = "${product.service.url:http://localhost:8082}")
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    ProductDTO getProduct(@PathVariable("id") String id);
}
