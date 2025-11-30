package com.blibli.training.product.controller;

import com.blibli.training.framework.dto.BaseResponse;
import com.blibli.training.product.entity.Product;
import com.blibli.training.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;

    @GetMapping
    public BaseResponse<List<Product>> getProducts() {
        return BaseResponse.success(productRepository.findAll());
    }

    @PostMapping
    public BaseResponse<Product> createProduct(@RequestBody Product product) {
        return BaseResponse.success(productRepository.save(product));
    }
}
