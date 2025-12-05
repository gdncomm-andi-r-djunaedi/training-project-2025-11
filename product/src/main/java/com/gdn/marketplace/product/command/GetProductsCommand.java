package com.gdn.marketplace.product.command;

import com.gdn.marketplace.product.entity.Product;
import com.gdn.marketplace.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class GetProductsCommand implements Command<Page<Product>, GetProductsCommand.Request> {

    @Autowired
    private ProductService productService;

    @Override
    public Page<Product> execute(Request request) {
        return productService.getProducts(request.getPage(), request.getSize());
    }

    public static class Request {
        private int page;
        private int size;

        public Request(int page, int size) {
            this.page = page;
            this.size = size;
        }

        public int getPage() {
            return page;
        }

        public int getSize() {
            return size;
        }
    }
}
