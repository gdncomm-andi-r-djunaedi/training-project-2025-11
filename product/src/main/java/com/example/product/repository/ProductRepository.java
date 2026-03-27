package com.example.product.repository;

import com.example.product.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<ProductEntity, String> {
    
    Optional<ProductEntity> findByItemSku(String itemSku);
    
    boolean existsByItemSku(String itemSku);
    
    @Query("{ $or: [ " +
           "{ 'productName': { $regex: ?0, $options: 'i' } }, " +
           "{ 'productDescription': { $regex: ?0, $options: 'i' } }, " +
           "{ 'itemSku': { $regex: ?0, $options: 'i' } } " +
           "] }")
    Page<ProductEntity> findBySearchTerm(String searchTerm, Pageable pageable);

    void deleteByItemSku(String itemSku);
}

