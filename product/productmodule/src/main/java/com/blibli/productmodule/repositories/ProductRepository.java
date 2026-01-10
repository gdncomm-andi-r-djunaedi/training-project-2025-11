package com.blibli.productmodule.repositories;

import com.blibli.productmodule.entity.ProductSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends MongoRepository<ProductSearch, Long> {

    Page<ProductSearch> findByNameContainingIgnoreCase(String searchTerm, Pageable pageable);

    ProductSearch findByProductCode(String productCode);

}