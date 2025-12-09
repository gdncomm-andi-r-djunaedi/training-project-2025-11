package com.gdn.training.product.controller;

import com.gdn.training.product.dto.ProductDetailResponse;
import com.gdn.training.product.dto.ProductListRequest;
import com.gdn.training.product.dto.SearchProductRequest;
import com.gdn.training.product.entity.Product;
import com.gdn.training.product.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/product-detail")
    public ResponseEntity<ProductDetailResponse> viewDetailById(@RequestParam String product_id) {
        return ResponseEntity.ok(productService.viewDetailById(product_id));
    }

    @PostMapping("/list")
    public ResponseEntity<Page<Product>> viewProductList(@RequestBody ProductListRequest request) {
        Page<Product> products = productService.viewProductList(request);
        return ResponseEntity.ok(products);
    }

    @PostMapping("/search")
    public ResponseEntity<Page<Product>> searchProduct(@RequestBody SearchProductRequest request) {
        Page<Product> products = productService.searchProduct(request);
        return ResponseEntity.ok(products);
    }
}
