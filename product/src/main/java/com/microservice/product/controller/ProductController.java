package com.microservice.product.controller;

import com.microservice.product.dto.ProductDto;
import com.microservice.product.dto.ProductResponseDto;
import com.microservice.product.dto.ProductSearchDto;
import com.microservice.product.exception.ValidationException;
import com.microservice.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    ProductService productService;

    @GetMapping
    public ResponseEntity<Page<ProductResponseDto>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponseDto> products = productService.getProducts(pageable);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/searchByTerm")
    public ResponseEntity<Page<ProductResponseDto>> getProductsBySearch(
            @RequestParam(required = true) String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        if(searchTerm == null || searchTerm.trim().isEmpty()){
            throw new ValidationException("Search term is required and cannot be empty");
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponseDto> products = productService.getProductsBySearch(searchTerm.trim(), pageable);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @PostMapping("/search")
    public ResponseEntity<Page<ProductResponseDto>> searchProducts(
            @RequestBody ProductSearchDto searchDto
    ){
        if(searchDto == null){
            throw new ValidationException("Search criteria is required");
        }
        Page<ProductResponseDto> products = productService.searchProducts(searchDto);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(
            @PathVariable Long id
    ){
        return new ResponseEntity<>(productService.getProductsById(id), HttpStatus.OK);
    }

    @PostMapping("/addProduct")
    public ResponseEntity<ProductResponseDto> addProduct(
    @RequestBody ProductDto productDto
    ){
        if(productDto.getSku() == null || productDto.getSku().isBlank() || productDto.getSku().isEmpty()){
            throw new ValidationException("Sku is required");
        }
        else if(productDto.getName() == null || productDto.getName().isEmpty() || productDto.getName().isBlank()) {
            throw new ValidationException("Sku Name is required");
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
        return new ResponseEntity<>(saved, HttpStatus.OK);
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductDto productDto
    ){
        if(id == null){
            throw new ValidationException("Id is required");
        }
        else if(productDto.getSku() == null || productDto.getSku().isBlank() || productDto.getSku().isEmpty()){
            throw new ValidationException("Sku is required");
        }
        else if(productDto.getName() == null || productDto.getName().isEmpty() || productDto.getName().isBlank()) {
            throw new ValidationException("Sku Name is required");
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

        ProductResponseDto saved = productService.updateProduct(id, productDto);
        return new ResponseEntity<>(saved, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Boolean> deleteById(
            @PathVariable Long id
    ){
        productService.deleteById(id);
        return new ResponseEntity<>(Boolean.TRUE, HttpStatus.OK);
    }

}
