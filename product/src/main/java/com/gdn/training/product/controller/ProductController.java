package com.gdn.training.product.controller;

import com.gdn.training.product.dto.ProductListRequest;
import com.gdn.training.product.dto.SearchProductRequest;
import com.gdn.training.product.entity.Product;
import com.gdn.training.product.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/product-detail")
    public ResponseEntity<Map<String, Object>> viewDetailById(@RequestParam String product_id) {
        Optional<Product> product = productService.viewDetailById(product_id);

        if (product.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "product not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("product_id", product.get().getProduct_id());
        response.put("product_name", product.get().getProduct_name());
        response.put("price", product.get().getPrice());
        response.put("description", product.get().getDescription());

        return ResponseEntity.status(HttpStatus.OK).body(response);
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
