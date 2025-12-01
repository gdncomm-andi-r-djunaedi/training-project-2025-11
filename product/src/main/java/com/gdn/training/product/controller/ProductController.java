package com.gdn.training.product.controller;

import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gdn.training.product.model.response.ProductDetailResponse;
import com.gdn.training.product.model.response.PagedResponse;
import com.gdn.training.product.service.ProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping("/search")
    public PagedResponse<ProductDetailResponse> searchProduct(@RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return productService.search(name, page, size);
    }

    @GetMapping("/list")
    public PagedResponse<ProductDetailResponse> getAllProduct(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return productService.getAll(page, size);
    }

    @GetMapping("/detail/{id}")
    public ProductDetailResponse viewProductDetail(@PathVariable @NonNull Long id) {
        return productService.getDetail(id);
    }

}
