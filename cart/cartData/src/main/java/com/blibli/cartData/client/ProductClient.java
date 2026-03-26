package com.blibli.cartData.client;

import com.blibli.cartData.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", url = "${product.service.url}")
public interface ProductClient {

    @GetMapping("/api/product/view/{productId}")
    ProductDTO getProductById(@PathVariable String productId);
}
