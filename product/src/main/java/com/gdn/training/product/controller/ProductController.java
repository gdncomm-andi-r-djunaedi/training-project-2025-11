package com.gdn.training.product.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gdn.training.product.model.response.ProductDetailResponse;
import com.gdn.training.product.model.response.PagedResponse;
import com.gdn.training.product.service.ProductService;

import jakarta.validation.constraints.Min;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Validated
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<PagedResponse<ProductDetailResponse>> getProducts(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page number must be greater than or equal to 0") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Page size must be greater than 0") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<ProductDetailResponse> response = productService.getAll(name, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<ProductDetailResponse> viewProductDetail(
            @PathVariable String id) {
        ProductDetailResponse response = productService.getDetail(id);
        return ResponseEntity.ok(response);
    }
}
