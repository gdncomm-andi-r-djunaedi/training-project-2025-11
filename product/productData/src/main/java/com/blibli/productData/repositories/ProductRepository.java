package com.blibli.productData.repositories;

import com.blibli.productData.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface ProductRepository extends MongoRepository<Product,String> {

    @Query("{ '$or': [ " +
            " { 'name': { $regex: ?0, $options: 'i' } }, " +
            " { 'description': { $regex: ?0, $options: 'i' } }, " +
            " { 'brand': { $regex: ?0, $options: 'i' } } " +
            "] }")
    Page<Product> searchProducts(String searchTerm, Pageable pageable);

    Product findByProductId(String productId);
}
