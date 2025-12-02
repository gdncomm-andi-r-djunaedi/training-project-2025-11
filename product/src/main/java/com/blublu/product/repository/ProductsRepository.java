package com.blublu.product.repository;

import com.blublu.product.document.Products;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface ProductsRepository extends MongoRepository<Products, Long> {
  @Query("{ 'name': { $regex: ?0, $options: 'i' } }")
  List<Products> findProductsByName(String productName, Pageable page);

}
