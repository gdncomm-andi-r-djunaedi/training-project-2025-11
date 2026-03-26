package com.blibli.cartData.controller;

import com.blibli.cartData.client.ProductClient;
import com.blibli.cartData.dto.ProductDTO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductClient productClient;

    @GetMapping("/view/{productId}")
    @Operation(summary = "View product", description = "View product detail by id")
    public ProductDTO viewProduct(@PathVariable String productId) {
        return productClient.getProductById(productId);
    }
}
