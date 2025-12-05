package com.gdn.marketplace.product.command;

import com.gdn.marketplace.product.entity.Product;
import com.gdn.marketplace.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GetProductByIdCommand implements Command<Product, String> {

    @Autowired
    private ProductService productService;

    @Override
    public Product execute(String id) {
        return productService.getProductById(id);
    }
}
