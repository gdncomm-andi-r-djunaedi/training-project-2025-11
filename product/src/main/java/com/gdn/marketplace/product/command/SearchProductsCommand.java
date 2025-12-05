package com.gdn.marketplace.product.command;

import com.gdn.marketplace.product.entity.Product;
import com.gdn.marketplace.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class SearchProductsCommand implements Command<Page<Product>, SearchProductsCommand.Request> {

    @Autowired
    private ProductService productService;

    @Override
    public Page<Product> execute(Request request) {
        return productService.searchProducts(request.getName(), request.getPage(), request.getSize());
    }

    public static class Request {
        private String name;
        private int page;
        private int size;

        public Request(String name, int page, int size) {
            this.name = name;
            this.page = page;
            this.size = size;
        }

        public String getName() {
            return name;
        }

        public int getPage() {
            return page;
        }

        public int getSize() {
            return size;
        }
    }
}
