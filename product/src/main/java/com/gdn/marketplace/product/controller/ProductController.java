package com.gdn.marketplace.product.controller;

import com.gdn.marketplace.product.command.*;
import com.gdn.marketplace.product.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private CreateProductCommand createProductCommand;

    @Autowired
    private GetProductsCommand getProductsCommand;

    @Autowired
    private GetProductByIdCommand getProductByIdCommand;

    @Autowired
    private SearchProductsCommand searchProductsCommand;

    @Autowired
    private UpdateProductCommand updateProductCommand;

    @Autowired
    private DeleteProductCommand deleteProductCommand;

    @PostMapping
    public Product addProduct(@RequestBody Product product) {
        return createProductCommand.execute(product);
    }

    @GetMapping
    public Page<Product> findAllProducts(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return getProductsCommand.execute(new GetProductsCommand.Request(page, size));
    }

    @GetMapping("/{id}")
    public Product findProductById(@PathVariable String id) {
        return getProductByIdCommand.execute(id);
    }

    @GetMapping("/search")
    public Page<Product> searchProducts(@RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return searchProductsCommand.execute(new SearchProductsCommand.Request(name, page, size));
    }

    @PutMapping
    public Product updateProduct(@RequestBody Product product) {
        return updateProductCommand.execute(product);
    }

    @DeleteMapping("/{id}")
    public String deleteProduct(@PathVariable String id) {
        return deleteProductCommand.execute(id);
    }
}
