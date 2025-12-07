package com.marketplace.product.command.impl;

import com.marketplace.common.exception.ResourceNotFoundException;
import com.marketplace.product.command.GetProductByIdCommand;
import com.marketplace.product.document.Product;
import com.marketplace.product.dto.request.GetProductByIdRequest;
import com.marketplace.product.repository.ProductRepository;
import com.marketplace.product.service.ProductCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetProductByIdCommandImpl implements GetProductByIdCommand {

    private final ProductRepository productRepository;
    private final ProductCacheService productCacheService;
    private final com.marketplace.common.mapper.MapperService mapperService;

    @Override
    public com.marketplace.product.dto.response.ProductResponse execute(GetProductByIdRequest request) {
        var productId = request.getProductId();

        log.info("Fetching product with ID: {}", productId);

        // Try cache first
        Product product = productCacheService.getProduct(productId)
                .orElseGet(() -> {
                    log.debug("Fetching product {} from database", productId);
                    Product dbProduct = productRepository.findById(productId)
                            .orElseThrow(() -> {
                                log.warn("Product not found with ID: {}", productId);
                                return new ResourceNotFoundException("Product", productId);
                            });

                    // Cache the result
                    productCacheService.cacheProduct(dbProduct);

                    return dbProduct;
                });

        return mapperService.map(product, com.marketplace.product.dto.response.ProductResponse.class);
    }
}
