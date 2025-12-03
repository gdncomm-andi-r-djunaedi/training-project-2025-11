package com.example.cart.controllers;

import com.example.cart.client.ProductClient;
import com.example.cart.dto.GetBulkProductResponseDTO;
import com.example.cart.utils.APIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductClient productClient;

    @PostMapping("/getBulk")
    public ResponseEntity<APIResponse<List<GetBulkProductResponseDTO>>> fetchProductInBulk(@RequestBody List<Long> productIds) {
        return productClient.fetchProductInBulk(productIds);
    }
}
