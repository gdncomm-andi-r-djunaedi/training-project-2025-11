package com.blibli.cartModule.client;

import com.blibli.productModule.dto.ApiResponse;
import com.blibli.productModule.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", url = "${product.service.url}")
public interface ProductServiceClient {

  @GetMapping("/api/products/detail/{productId}")
  ResponseEntity<ApiResponse<ProductDto>> getProductById(
      @PathVariable String productId);
}

