package com.ecom.cart.client;

import com.ecom.cart.Dto.ApiResponse;
import com.ecom.cart.Dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", url = "http://localhost:8383")
public interface ProductClient {

    @GetMapping("/products/get/{sku}")
    ApiResponse<ProductDto> getProductBySku(@PathVariable("sku") String sku);

}
