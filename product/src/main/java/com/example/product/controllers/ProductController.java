package com.example.product.controllers;

import com.example.product.dto.GetBulkProductResponseDTO;
import com.example.product.dto.ProductRequestDTO;
import com.example.product.dto.ProductResponseDTO;
import com.example.product.entity.Product;
import com.example.product.service.ProductService;
import com.example.product.utils.APIResponse;
import com.example.product.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping("/create")
    public ResponseEntity<APIResponse<ProductResponseDTO>> createProduct(@RequestBody ProductRequestDTO productRequestDTO) {
        APIResponse<ProductResponseDTO> response = ResponseUtil.success(
                productService.createProduct(productRequestDTO)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/productId/{productId}")
    public ResponseEntity<APIResponse<ProductResponseDTO>> getProductByProductId(@PathVariable long productId) {
        APIResponse<ProductResponseDTO> response = ResponseUtil.success(
                productService.getProductByProductId(productId)
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<APIResponse<List<ProductResponseDTO>>> getProductsByCategory(@PathVariable String category) {
        APIResponse<List<ProductResponseDTO>> response = ResponseUtil.success(
                productService.getProductsByCategory(category)
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<APIResponse<List<ProductResponseDTO>>> searchProductsByTitle(
            @RequestParam String title) {
        APIResponse<List<ProductResponseDTO>> response = ResponseUtil.success(
                productService.searchProductsByTitle(title)
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/{productId}")
    public ResponseEntity<APIResponse<ProductResponseDTO>> updateProduct(
            @PathVariable long productId,
            @RequestBody ProductRequestDTO updateProductDTO) {
        APIResponse<ProductResponseDTO> response = ResponseUtil.success(
                productService.updateProduct(productId, updateProductDTO)
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/remove/{id}")
    public ResponseEntity<APIResponse<String>> deleteByProductId(@PathVariable long id) {
        String message = productService.deleteProductByProductId(id);
        APIResponse<String> response = ResponseUtil.success(message);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/getBulk")
    public ResponseEntity<APIResponse<List<GetBulkProductResponseDTO>>> fetchProductInBulk(
            @RequestBody List<Long> productIds) {
        APIResponse<List<GetBulkProductResponseDTO>> response = ResponseUtil.success(
                productService.getProductsInBulk(productIds)
        );
        return ResponseEntity.ok(response);
    }

}
