package com.gdn.training.product.infrastructure.usecase;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.gdn.training.product.application.dto.ProductResponse;
import com.gdn.training.product.application.usecase.GetProductByIdUseCase;
import com.gdn.training.product.domain.exception.ProductNotFoundException;
import com.gdn.training.product.domain.model.Product;
import com.gdn.training.product.domain.repository.ProductRepository;

/**
 * Implementation of GetProductByIdUseCase — infrastructure bean that
 * orchestrates domain repository.
 */
@Service
public class GetProductByIdUseCaseImpl implements GetProductByIdUseCase {

    // attributes
    private final ProductRepository productRepository;

    // constructor
    public GetProductByIdUseCaseImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // methods
    @Override
    public ProductResponse execute(String id) throws ProductNotFoundException {
        Optional<Product> product = productRepository.findById(UUID.fromString(id));
        if (product.isEmpty()) {
            throw new ProductNotFoundException(id);
        }
        return new ProductResponse(
                product.get().getProductId().toString(),
                product.get().getSellerId().toString(),
                product.get().getName(),
                product.get().getDescription(),
                product.get().getCategory(),
                product.get().getPrice(),
                product.get().getStock(),
                product.get().getStatus().toString(),
                product.get().getCreatedAt().toEpochMilli());
    }
}
