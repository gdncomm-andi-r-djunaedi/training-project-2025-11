package com.sc.productservice.controller;

import com.sc.productservice.dto.ProductDto;
import com.sc.productservice.service.ProductService;
import com.sc.utilsservice.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<ProductDto>> createProduct(@RequestBody ProductDto dto) {
        ApiResponse<ProductDto> response = productService.createProduct(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{productCode}")
    public ResponseEntity<ApiResponse<ProductDto>> getProduct(@PathVariable String productCode) {
        ApiResponse<ProductDto> response = productService.getProduct(productCode);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Page<ProductDto>>> getAllProducts(@RequestParam int page,
                                                                        @RequestParam int size) {
        Pageable pageable = PageRequest.of(page, size);
        ApiResponse<Page<ProductDto>> response = productService.getAllProducts(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ProductDto>>> searchProducts(@RequestParam String keyword,
                                                                        @RequestParam int page,
                                                                        @RequestParam int size) {
        Pageable pageable = PageRequest.of(page, size);
        ApiResponse<Page<ProductDto>> response = productService.searchProducts(keyword, pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<ProductDto>> updateProduct(@RequestBody ProductDto dto) {
        ApiResponse<ProductDto> response = productService.updateProduct(dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{productCode}")
    public ResponseEntity<ApiResponse<String>> deleteProduct(@PathVariable String productCode) {
        ApiResponse<String> response = productService.deleteProduct(productCode);
        return ResponseEntity.ok(response);
    }


}
