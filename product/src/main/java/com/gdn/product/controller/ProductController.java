package com.gdn.product.controller;

import com.gdn.product.dto.request.ProductDTO;
import com.gdn.product.dto.request.SearchProductDTO;
import com.gdn.product.dto.response.ApiResponse;
import com.gdn.product.dto.response.ProductSearchResponseDTO;
import com.gdn.product.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<ProductDTO>> register(@RequestBody ProductDTO dto) {
        log.info("Create product request: {}", dto);

        ProductDTO created = productService.createProduct(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully", created));
    }


    @PutMapping("/update")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(@RequestBody ProductDTO dto) {
        log.info("Update product request: {}", dto);

        ProductDTO updated = productService.update(dto);

        return ResponseEntity.ok(
                ApiResponse.success("Product updated successfully", updated)
        );
    }


    @PostMapping("/search")
    public ResponseEntity<ApiResponse<ProductSearchResponseDTO>> searchProducts(
            @RequestBody SearchProductDTO request
    ) {
        log.info("Search products request: {}", request);

        ProductSearchResponseDTO result = productService.search(request);

        return ResponseEntity.ok(
                ApiResponse.success("Search success", result)
        );
    }


    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductDetail(@PathVariable String productId) {
        log.info("Get product detail, productId={}", productId);

        ProductDTO dto = productService.getProductDetail(productId);

        return ResponseEntity.ok(
                ApiResponse.success("Product detail fetched successfully", dto)
        );
    }
}
