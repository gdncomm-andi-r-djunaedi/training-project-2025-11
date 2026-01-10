package com.blibli.productmodule.controller;

import com.blibli.productmodule.dto.Productdto;
import com.blibli.productmodule.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    ProductService productService;

    @GetMapping("/search/{searchTerm}")
    public ResponseEntity<Page<Productdto>> searchProducts(
            @PathVariable String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        validatePagination(page, size);
        Page<Productdto> result = productService.searchProducts(searchTerm, page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/list")
    public ResponseEntity<Page<Productdto>> listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        validatePagination(page, size);
        Page<Productdto> result = productService.listProduct(page, size);
        return ResponseEntity.ok(result);
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid pagination value: page must be greater than or equal to 0"
            );
        }
        if (size <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid pagination value: size must be greater than 0"
            );
        }
        if (size > 50) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid pagination value: size must be less than or equal to 50"
            );
        }
    }

    @GetMapping("/productDetail/{productId}")
    public ResponseEntity<Productdto> getproductDetails(
            @PathVariable String productId) {
        Productdto result = productService.productDetail(productId);
        return ResponseEntity.ok(result);
    }

}
