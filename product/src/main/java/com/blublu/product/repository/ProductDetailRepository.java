package com.blublu.product.repository;

import com.blublu.product.document.Product;
import com.blublu.product.document.ProductDetail;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface ProductDetailRepository extends MongoRepository<ProductDetail, Long> {
  @Query("{name: {$like:':productName'}}")
  ProductDetail findProductByName(String productName);

}
