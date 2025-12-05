package com.gdn.marketplace.product.command;

import com.gdn.marketplace.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeleteProductCommand implements Command<String, String> {

    @Autowired
    private ProductService productService;

    @Override
    public String execute(String id) {
        return productService.deleteProduct(id);
    }
}
