package com.marketplace.product.command;

import com.marketplace.common.command.Command;
import com.marketplace.product.document.Product;
import com.marketplace.product.dto.request.SearchProductsRequest;
import com.marketplace.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchProductsCommand implements Command<SearchProductsRequest, Page<Product>> {

    private final ProductService productService;

    @Override
    public Page<Product> execute(SearchProductsRequest request) {
        return productService.searchProducts(request.getName(), request.getPageable());
    }
}
