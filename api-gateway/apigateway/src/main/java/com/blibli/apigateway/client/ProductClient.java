package com.blibli.apigateway.client;

import com.blibli.apigateway.dto.response.PageResponse;
import com.blibli.apigateway.dto.request.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "product-service", url = "${product.service.url}")
public interface ProductClient {

    @GetMapping("/api/products/list")
    PageResponse<ProductDto> listOfProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size);

    @GetMapping("/api/products/search/{searchTerm}")
    PageResponse<ProductDto> searchProducts(
            @PathVariable String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size);

    @GetMapping("/api/products/productDetail/{productId}")
    ProductDto getProductDetails(@PathVariable String productId);

}

