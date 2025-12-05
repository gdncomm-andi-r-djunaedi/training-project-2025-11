package com.microservice.product.controller;

import com.microservice.product.dto.ProductDto;
import com.microservice.product.dto.ProductResponseDto;
import com.microservice.product.exception.ValidationException;
import com.microservice.product.service.ProductService;
import com.microservice.product.wrapper.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@Slf4j
public class ProductController {

    @Autowired
    ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponseDto>>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        log.info("GET /api/products - Request received. Page: {}, Size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponseDto> products = productService.getProducts(pageable);
        ApiResponse<Page<ProductResponseDto>> response = ApiResponse.success(products, HttpStatus.OK);
        log.info("GET /api/products - Response sent successfully. Total elements: {}", products.getTotalElements());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/searchByTerm")
    public ResponseEntity<ApiResponse<Page<ProductResponseDto>>> getProductsBySearch(
            @RequestParam(required = true) String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        log.info("GET /api/products/searchByTerm - Request received. SearchTerm: '{}', Page: {}, Size: {}",
                searchTerm, page, size);
        if(searchTerm == null || searchTerm.trim().isEmpty()){
            throw new ValidationException("Search term is required and cannot be empty");
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponseDto> products = productService.getProductsBySearch(searchTerm.trim(), pageable);
        ApiResponse<Page<ProductResponseDto>> response = ApiResponse.success(products, HttpStatus.OK);
        log.info("GET /api/products/searchByTerm - Response sent successfully. Found {} products",
                products.getTotalElements());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{skuId}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> getProductById(
            @PathVariable String skuId  // Changed from Long to String
    ){
        log.info("GET /api/products/{} - Request received", skuId);
        ProductResponseDto product = productService.getProductsById(skuId);
        ApiResponse<ProductResponseDto> response = ApiResponse.success(product, HttpStatus.OK);
        log.info("GET /api/products/{} - Response sent successfully. Product SKU: {}",
                skuId, product.getSkuId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/getSkusById")
    public ResponseEntity<ApiResponse<List<ProductResponseDto>>> getProductsBySkuIds(
            @RequestBody List<String> skuIds
    ){
        log.info("POST /api/products/getSkusById - Request received. SKU IDs count: {}, SKUs: {}",
                skuIds != null ? skuIds.size() : 0, skuIds);
        if (skuIds == null || skuIds.isEmpty()) {
            throw new ValidationException("SKU IDs list cannot be null or empty");
        }
        List<ProductResponseDto> products = productService.getProductsBySkuIds(skuIds);
        ApiResponse<List<ProductResponseDto>> response = ApiResponse.success(products, HttpStatus.OK);
        log.info("POST /api/products/getSkusById - Response sent successfully. Found {} products",
                products.size());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/isPresent")
    public ResponseEntity<ApiResponse<Boolean>> isProductIdPresent(
            @RequestParam String skuId  // Changed from Long to String
    ){
        log.info("GET /api/products/isPresent - Request received. SKU ID: {}", skuId);
        Boolean isPresent = productService.isProductIdPresent(skuId);
        ApiResponse<Boolean> response = ApiResponse.success(isPresent, HttpStatus.OK);
        log.info("GET /api/products/isPresent - Response sent. Product exists: {}", isPresent);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/addProduct")
    public ResponseEntity<ApiResponse<ProductResponseDto>> addProduct(
            @RequestBody ProductDto productDto
    ){
        log.info("POST /api/products/addProduct - Request received. SKU: {}, Name: {}",
                productDto.getSkuId(), productDto.getName());
        if(productDto.getSkuId() == null || productDto.getSkuId().isBlank() || productDto.getSkuId().isEmpty()){
            throw new ValidationException("skuId is required");
        }
        else if(productDto.getName() == null || productDto.getName().isEmpty() || productDto.getName().isBlank()) {
            throw new ValidationException("skuId Name is required");
        }

        else if(productDto.getPrice() == null || productDto.getPrice() <= 0 || productDto.getPrice().toString().isBlank() || productDto.getPrice().toString().isEmpty()) {
            throw new ValidationException("Price is required");
        }

        if (productDto.getItemCode() == null || productDto.getItemCode() <= 0) {
            throw new ValidationException("Item code is required and must be > 0");
        }

        if (productDto.getLength() == null || productDto.getLength() <= 0) {
            throw new ValidationException("Length must be greater than 0");
        }

        if (productDto.getHeight() == null || productDto.getHeight() <= 0) {
            throw new ValidationException("Height must be greater than 0");
        }

        if (productDto.getWidth() == null || productDto.getWidth() <= 0) {
            throw new ValidationException("Width must be greater than 0");
        }

        if (productDto.getWeight() == null || productDto.getWeight() <= 0) {
            throw new ValidationException("Weight must be greater than 0");
        }

        if (productDto.getDangerousLevel() == null) {
            throw new ValidationException("Dangerous level cannot be null");
        }

        ProductResponseDto saved = productService.addProduct(productDto);
        ApiResponse<ProductResponseDto> response = ApiResponse.success(saved, HttpStatus.CREATED);
        log.info("POST /api/products/addProduct - Product created successfully. SKU: {}",
                saved.getSkuId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/update/{skuId}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> updateProduct(
            @PathVariable String skuId,  // Changed from Long to String
            @RequestBody ProductDto productDto
    ){
        log.info("POST /api/products/update/{} - Request received. New SKU: {}, New Name: {}",
                skuId, productDto.getSkuId(), productDto.getName());
        if(productDto.getSkuId() == null || productDto.getSkuId().isBlank() || productDto.getSkuId().isEmpty()){
            throw new ValidationException("skuId is required");
        }
        else if(productDto.getName() == null || productDto.getName().isEmpty() || productDto.getName().isBlank()) {
            throw new ValidationException("skuId Name is required");
        }

        else if(productDto.getPrice() == null || productDto.getPrice() <= 0 || productDto.getPrice().toString().isBlank() || productDto.getPrice().toString().isEmpty()) {
            throw new ValidationException("Price is required");
        }

        else if (productDto.getItemCode() == null || productDto.getItemCode() <= 0) {
            throw new ValidationException("Item code is required and must be > 0");
        }

        else if (productDto.getLength() == null || productDto.getLength() <= 0) {
            throw new ValidationException("Length must be greater than 0");
        }

        else if (productDto.getHeight() == null || productDto.getHeight() <= 0) {
            throw new ValidationException("Height must be greater than 0");
        }

        else if (productDto.getWidth() == null || productDto.getWidth() <= 0) {
            throw new ValidationException("Width must be greater than 0");
        }

        else if (productDto.getWeight() == null || productDto.getWeight() <= 0) {
            throw new ValidationException("Weight must be greater than 0");
        }

        else if (productDto.getDangerousLevel() == null) {
            throw new ValidationException("Dangerous level cannot be null");
        }

        ProductResponseDto saved = productService.updateProduct(skuId, productDto);
        ApiResponse<ProductResponseDto> response = ApiResponse.success(saved, HttpStatus.OK);
        log.info("POST /api/products/update/{} - Product updated successfully. SKU: {}",
                skuId, saved.getSkuId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{skuId}")
    public ResponseEntity<ApiResponse<Boolean>> deleteById(
            @PathVariable String skuId
    ){
        log.info("DELETE /api/products/delete/{} - Request received", skuId);
        productService.deleteById(skuId);
        ApiResponse<Boolean> response = ApiResponse.success(true, HttpStatus.OK);
        log.info("DELETE /api/products/delete/{} - Product deleted successfully", skuId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}