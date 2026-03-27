package com.blibli.ProductService.controller;


import com.blibli.ProductService.dto.ProductDto;
import com.blibli.ProductService.service.ProductService;
import com.blibli.ProductService.util.ApiResponse;
import com.blibli.ProductService.util.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/getProducts")
    public ResponseEntity<ApiResponse<ProductDto>> getProductsById(@RequestParam("productId") String productId){
        ProductDto product = productService.getProductById(productId);
        return ResponseUtil.success("Products fetched Successfully",product);
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<ProductDto>> addProducts(@RequestBody ProductDto productDto){
        ProductDto result = productService.createProduct(productDto);
        return ResponseUtil.created("Product is created Successfully",result);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteProductById(@RequestParam String productId){
        productService.deleteById(productId);
        return ResponseUtil.success("Product deleted Successfully");
    }

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<Integer>> syncAllProductsToElasticsearch() {
        int syncedCount = productService.syncAllProductsToElasticsearch();
        return ResponseUtil.success("Products synced to Elasticsearch successfully",syncedCount);
    }

}