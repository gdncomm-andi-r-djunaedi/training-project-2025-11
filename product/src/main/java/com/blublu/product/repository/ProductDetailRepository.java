package com.blublu.product.repository;

import com.blublu.product.document.ProductDetail;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

@Component
public interface ProductDetailRepository extends MongoRepository<ProductDetail, Long> {
  ProductDetail findProductBySkuCode(String skuCode);

}
