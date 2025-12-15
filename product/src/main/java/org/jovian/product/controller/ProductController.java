package org.jovian.product.controller;

import lombok.RequiredArgsConstructor;
import org.jovian.product.dto.ProductRequest;
import org.jovian.product.entity.Product;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.jovian.product.service.ProductService;
import org.jovian.common.dto.ProductDTO;
import org.jovian.product.dto.ProductDetailsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable String id) {
        Product product = productService.findById(id);
        return ResponseEntity.ok(productService.toDTO(product));
    }

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductRequest productRequest) {
        ProductDTO createdProduct = productService.createProduct(productRequest);
        return ResponseEntity.ok(createdProduct);
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<ProductDetailsResponse> viewProductDetails(
            @PathVariable String id
    ) {
        ProductDetailsResponse response = productService.viewProductDetails(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<ProductDTO>> getAllProducts(Pageable pageable) {
        Page<Product> page = productService.findAll(pageable);

        Page<ProductDTO> dtoPage = new PageImpl<>(
                page.getContent().stream()
                        .map(productService::toDTO)
                        .collect(Collectors.toList()),
                pageable,
                page.getTotalElements()
        );

        return ResponseEntity.ok(dtoPage);
    }

    public ResponseEntity<Page<ProductDTO>> searchProducts(
            @RequestParam(required = false) String keyword,
            Pageable pageable
    ) {
        Page<Product> page = productService.searchProducts(keyword, pageable);

        Page<ProductDTO> dtoPage = new PageImpl<>(
                page.getContent().stream()
                        .map(productService::toDTO)
                        .collect(Collectors.toList()),
                pageable,
                page.getTotalElements()
        );

        return ResponseEntity.ok(dtoPage);
    }
}
