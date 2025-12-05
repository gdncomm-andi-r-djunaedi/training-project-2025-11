package com.blibli.cartmodule.client;

import com.blibli.cartmodule.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "product-service",
    url = "${product.service.url:http://localhost:8082}"
)
public interface ProductClient {

    @GetMapping("/api/products/productDetail/{productId}")
    ProductDto getProductDetails(@PathVariable("productId") String productCode);

}

