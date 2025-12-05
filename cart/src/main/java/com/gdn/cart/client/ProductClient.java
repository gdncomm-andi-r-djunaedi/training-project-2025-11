package com.gdn.cart.client;

import com.gdn.cart.dto.response.ApiResponse;
import com.gdn.cart.dto.response.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "products",
        url = "http://localhost:8086"
)
public interface ProductClient {

    @GetMapping("/products/{productId}")
    ApiResponse<ProductDTO> getProductById(@PathVariable("productId") String productId);

}
