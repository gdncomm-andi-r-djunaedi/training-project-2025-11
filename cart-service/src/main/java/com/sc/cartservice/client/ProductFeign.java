package com.sc.cartservice.client;

import com.sc.productservice.dto.ProductDto;
import com.sc.utilsservice.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", url = "http://localhost:8092/product")
public interface ProductFeign {

    @GetMapping("/{productCode}")
    ResponseEntity<ApiResponse<ProductDto>> getProduct(@PathVariable String productCode);
}
