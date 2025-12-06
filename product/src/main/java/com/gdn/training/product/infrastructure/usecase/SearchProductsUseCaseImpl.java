package com.gdn.training.product.infrastructure.usecase;

import com.gdn.training.product.application.dto.ProductResponse;
import com.gdn.training.product.application.usecase.SearchProductsUseCase;
import com.gdn.training.product.domain.model.Product;
import com.gdn.training.product.domain.repository.ProductRepository;
import com.gdn.training.product.domain.specification.ProductSpecification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Search use case implementation. Delegates to domain repository and maps
 * domain -> DTO.
 */
@Service
public class SearchProductsUseCaseImpl implements SearchProductsUseCase {

    // attributes
    private final ProductRepository productRepository;

    // constructor
    public SearchProductsUseCaseImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // methods
    @Override
    public List<ProductResponse> execute(ProductSpecification spec, int page, int size) {
        List<Product> products = productRepository.search(spec, page, size, null);
        return products.stream().map(p -> new ProductResponse(
                p.getProductId().toString(),
                p.getSellerId().toString(),
                p.getName(),
                p.getDescription(),
                p.getCategory(),
                p.getPrice(),
                p.getStock(),
                p.getStatus().name(),
                p.getCreatedAt().toEpochMilli())).collect(Collectors.toList());
    }
}
