package com.example.cart.client;

import com.example.cart.dto.GetBulkProductResponseDTO;
import com.example.cart.utils.APIResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "product", url = "${product.service.url:http://localhost:6060}")
public interface ProductClient {

    @PostMapping("/api/products/getBulk")
    ResponseEntity<APIResponse<List<GetBulkProductResponseDTO>>> fetchProductInBulk(@RequestBody List<Long> productIds);
}
