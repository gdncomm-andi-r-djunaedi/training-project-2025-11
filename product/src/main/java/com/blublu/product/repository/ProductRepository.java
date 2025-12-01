package com.blublu.product.repository;

import com.blublu.product.document.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ProductRepository extends MongoRepository<Product, Long> {
  @Query("{name: {$like:':productName'}}")
  List<Product> findProductByName(String productName, Pageable page);

}
