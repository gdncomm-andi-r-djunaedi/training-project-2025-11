package com.blibli.CartService.client;

import com.blibli.CartService.dto.ProductResponseDto;
import com.blibli.CartService.util.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name = "product-service", url = "http://localhost:8088")
public interface ProductFeignClient {

    @GetMapping("/products/getProducts")
    ApiResponse<ProductResponseDto> getProduct(@RequestParam("productId") String productId);
}
