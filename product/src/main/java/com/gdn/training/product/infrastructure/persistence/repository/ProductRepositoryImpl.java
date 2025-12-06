package com.gdn.training.product.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.gdn.training.product.domain.model.Product;
import com.gdn.training.product.domain.repository.ProductRepository;
import com.gdn.training.product.domain.specification.ProductSpecification;
import com.gdn.training.product.infrastructure.persistence.jpa.ProductJpaRepository;
import com.gdn.training.product.infrastructure.persistence.mapper.ProductMapper;
import com.gdn.training.product.infrastructure.search.SolrProductSearchRepository;

/**
 * ProductRepositoryImpl class
 */
@Repository
public class ProductRepositoryImpl implements ProductRepository {
    // attributes
    private final ProductJpaRepository productJpaRepository;
    private final SolrProductSearchRepository solrProductSearchRepository;

    // constructor
    public ProductRepositoryImpl(ProductJpaRepository productJpaRepository,
            SolrProductSearchRepository solrProductSearchRepository) {
        this.productJpaRepository = productJpaRepository;
        this.solrProductSearchRepository = solrProductSearchRepository;
    }

    // methods
    @Override
    public Optional<Product> findById(UUID id) {
        return productJpaRepository.findById(id)
                .map(ProductMapper.INSTANCE::toDomain);
    }

    @Override
    public List<Product> search(ProductSpecification spec, int page, int size, String sort) {
        return solrProductSearchRepository.search(spec, page, size, sort);
    }

    @Override
    public long count(ProductSpecification spec) {
        return solrProductSearchRepository.count(spec);
    }
}
