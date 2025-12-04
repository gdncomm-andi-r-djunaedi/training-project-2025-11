package com.marketplace.product.command;

import com.marketplace.common.command.Command;
import com.marketplace.product.document.Product;
import com.marketplace.product.dto.request.GetProductByIdRequest;
import com.marketplace.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetProductByIdCommand implements Command<GetProductByIdRequest, Product> {

    private final ProductService productService;

    @Override
    public Product execute(GetProductByIdRequest request) {
        return productService.getProductById(request.getProductId());
    }
}
