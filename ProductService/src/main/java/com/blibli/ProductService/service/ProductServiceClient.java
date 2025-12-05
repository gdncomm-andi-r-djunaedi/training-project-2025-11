package com.blibli.ProductService.service;

import com.blibli.ProductService.entity.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", url = "${product.service.url}")
public interface ProductServiceClient {

    @GetMapping("/api/products/{id}")
    Product getProductById(@PathVariable String id);

}
