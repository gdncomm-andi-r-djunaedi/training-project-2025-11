package com.dev.onlineMarketplace.cart.client;

import com.dev.onlineMarketplace.cart.dto.GdnResponseData;
import com.dev.onlineMarketplace.cart.dto.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "product-service", url = "${product.service.url:http://localhost:8062}")
public interface ProductServiceFeignClient {

    @GetMapping("/api/v1/products/{productId}")
    GdnResponseData<Product> getProductById(
            @PathVariable("productId") String productId
    );
}

