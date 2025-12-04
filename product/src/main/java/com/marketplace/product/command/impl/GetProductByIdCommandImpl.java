package com.marketplace.product.command.impl;

import com.marketplace.product.command.GetProductByIdCommand;
import com.marketplace.product.document.Product;
import com.marketplace.product.dto.request.GetProductByIdRequest;
import com.marketplace.product.exception.ProductNotFoundException;
import com.marketplace.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetProductByIdCommandImpl implements GetProductByIdCommand {

    private final ProductRepository productRepository;

    @Override
    public Product execute(GetProductByIdRequest request) {
        var productId = request.getProductId();
        
        log.info("Fetching product with ID: {}", productId);
        
        return productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("Product not found with ID: {}", productId);
                    return new ProductNotFoundException(productId);
                });
    }
}

