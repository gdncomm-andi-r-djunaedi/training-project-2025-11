package com.blibli.cart.Feign;

import com.blibli.cart.config.FeignConfig;
import com.blibli.product.dto.CreateProductResponseDTO;
import com.blibli.product.response.GdnResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "product",    configuration = FeignConfig.class,url = "http://localhost:8082/api")
public interface ProductFeign {
    @GetMapping("/product/getProductById/{productId}")
    ResponseEntity<GdnResponse<CreateProductResponseDTO>> getProductProductById(@PathVariable("productId") String productId);

    @PostMapping("/product/findByListProductId")
    ResponseEntity<GdnResponse<List<CreateProductResponseDTO>>> findByListProductId(@RequestBody List<String> productIds);
}
