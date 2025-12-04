package com.gdn.marketplace.product.command;

import com.gdn.marketplace.product.entity.Product;
import com.gdn.marketplace.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreateProductCommand implements Command<Product, Product> {

    @Autowired
    private ProductService productService;

    @Override
    public Product execute(Product request) {
        return productService.saveProduct(request);
    }
}
