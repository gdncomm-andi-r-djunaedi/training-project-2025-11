package com.blibli.apiGateway.controller;

import com.blibli.apiGateway.client.ProductClient;
import com.blibli.apiGateway.dto.ProductDTO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductClient productClient;

    @GetMapping("/view/{productId}")
    @Operation(summary = "View product", description = "View product detail by id")
    public ProductDTO viewProduct(@PathVariable String productId) {
        return productClient.viewProductById(productId);
    }

    @GetMapping("/search/{searchTerm}")
    @Operation(summary = "Search product", description = "Search product")
    public Page<ProductDTO> searchProduct(@PathVariable String searchTerm,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        return productClient.search(searchTerm,page,size);
    }

    @GetMapping("/list")
    @Operation(summary = "List products", description = "List all product details")
    public Page<ProductDTO> listProducts(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "20") int size) {
        return productClient.listAllProducts(page,size);
    }
}
