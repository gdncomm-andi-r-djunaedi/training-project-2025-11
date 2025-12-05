package com.example.api_gateway.client;

import com.example.api_gateway.response.ProductListResponse;
import com.example.api_gateway.request.ProductRequest;
import com.example.api_gateway.response.ProductResponse;
import com.example.api_gateway.request.UpdateProductRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "product", url = "${services.product.url}")
public interface ProductServiceClient {

    @PostMapping("/api/products/addProducts")
    ResponseEntity<List<ProductResponse>> addProducts(@RequestBody List<ProductRequest> productRequests);

    @GetMapping("/api/products/listing")
    ResponseEntity<ProductListResponse> getProductsListing(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize);

    @GetMapping("/api/products/detail/{itemSku}")
    ResponseEntity<ProductResponse> getProductDetailByItemSku(@PathVariable String itemSku);

    @GetMapping("/api/products/search")
    ResponseEntity<ProductListResponse> getProductsBySearchTerm(
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize);

    @PutMapping("/api/products/update/{itemSku}")
    ResponseEntity<ProductResponse> updateProduct(
            @PathVariable String itemSku,
            @RequestBody UpdateProductRequest updateRequest);

    @DeleteMapping("/api/products/deleteProductByItemSku")
    public void deleteProductByItemSku(@RequestParam("itemSku")String itemSku);
}

