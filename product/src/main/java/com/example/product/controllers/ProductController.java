package com.example.product.controllers;

import com.example.product.dto.GetBulkProductResponseDTO;
import com.example.product.dto.ProductRequestDTO;
import com.example.product.dto.ProductResponseDTO;
import com.example.product.service.ProductService;
import com.example.product.utils.APIResponse;
import com.example.product.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
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
                HttpStatus.CREATED.value(),
                HttpStatus.CREATED,
                productService.createProduct(productRequestDTO)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

//    @GetMapping("/{id}")
//    public ResponseEntity<APIResponse<ProductResponseDTO>> getProductById(@PathVariable String id) {
//        APIResponse<ProductResponseDTO> response = ResponseUtil.success(
//                HttpStatus.OK.value(),
//                HttpStatus.OK,
//                productService.getProductById(id)
//        );
//        return ResponseEntity.ok(response);
//    }

    @GetMapping("/productId/{productId}")
    public ResponseEntity<APIResponse<ProductResponseDTO>> getProductByProductId(@PathVariable long productId) {
        APIResponse<ProductResponseDTO> response = ResponseUtil.success(
                HttpStatus.OK.value(),
                HttpStatus.OK,
                productService.getProductByProductId(productId)
        );
        return ResponseEntity.ok(response);
    }

//    @GetMapping
//    public ResponseEntity<APIResponse<List<ProductResponseDTO>>> getAllProducts() {
//        APIResponse<List<ProductResponseDTO>> response = ResponseUtil.success(
//                HttpStatus.OK.value(),
//                HttpStatus.OK,
//                productService.getAllProducts()
//        );
//        return ResponseEntity.ok(response);
//    }

    @GetMapping("/category/{category}")
    public ResponseEntity<APIResponse<List<ProductResponseDTO>>> getProductsByCategory(@PathVariable String category) {
        APIResponse<List<ProductResponseDTO>> response = ResponseUtil.success(
                HttpStatus.OK.value(),
                HttpStatus.OK,
                productService.getProductsByCategory(category)
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<APIResponse<List<ProductResponseDTO>>> searchProductsByTitle(
            @RequestParam String title) {
        APIResponse<List<ProductResponseDTO>> response = ResponseUtil.success(
                HttpStatus.OK.value(),
                HttpStatus.OK,
                productService.searchProductsByTitle(title)
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/{productId}")
    public ResponseEntity<APIResponse<ProductResponseDTO>> updateProduct(
            @PathVariable long productId,
            @RequestBody ProductRequestDTO updateProductDTO) {
        APIResponse<ProductResponseDTO> response = ResponseUtil.success(
                HttpStatus.OK.value(),
                HttpStatus.OK,
                productService.updateProduct(productId, updateProductDTO)
        );
        return ResponseEntity.ok(response);
    }

//    @DeleteMapping("/remove/{id}")
//    public ResponseEntity<APIResponse<Void>> deleteProduct(@PathVariable String id) {
//        productService.deleteProduct(id);
//        APIResponse<Void> response = ResponseUtil.success(
//                HttpStatus.NO_CONTENT.value(),
//                HttpStatus.NO_CONTENT,
//                null
//        );
//        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
//    }

    @DeleteMapping("/remove/{id}")
    public ResponseEntity<APIResponse<Void>> deleteByProductId(@PathVariable long id) {
        productService.deleteProductByProductId(id);
        APIResponse<Void> response = ResponseUtil.success(
                HttpStatus.NO_CONTENT.value(),
                HttpStatus.NO_CONTENT,
                null
        );
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }

    @GetMapping("/getBulk")
    public ResponseEntity<APIResponse<List<GetBulkProductResponseDTO>>> fetchProductInBulk(
            @RequestParam List<Long> productIds) {
        APIResponse<List<GetBulkProductResponseDTO>> response = ResponseUtil.success(
                HttpStatus.OK.value(),
                HttpStatus.OK,
                productService.getProductsInBulk(productIds)
        );
        return ResponseEntity.ok(response);
    }
}
