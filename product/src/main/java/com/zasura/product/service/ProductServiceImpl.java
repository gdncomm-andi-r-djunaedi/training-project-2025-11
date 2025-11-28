package com.zasura.product.service;

import com.zasura.product.dto.ProductSearchRequest;
import com.zasura.product.entity.Product;
import com.zasura.product.exception.ProductNotFoundException;
import com.zasura.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
  @Autowired
  private ProductRepository productRepository;
  @Autowired
  private MongoTemplate mongoTemplate;

  @Override
  public Product getProductDetail(String productId) {
    Product product = productRepository.findById(productId).orElse(null);
    if (product == null) {
      throw new ProductNotFoundException("Product with ID " + productId + " not found");
    }
    return product;
  }

  @Override
  public Product createProduct(Product productRequest) {
    return productRepository.save(productRequest);
  }

  @Override
  public List<Product> searchProducts(ProductSearchRequest productSearchRequest) {
    List<Criteria> criteriaList = new ArrayList<>();

    if (productSearchRequest.getName() != null && !productSearchRequest.getName().isEmpty()) {
      criteriaList.add(Criteria.where("name")
          .regex(".*" + productSearchRequest.getName() + ".*", "i"));
    }

    if (productSearchRequest.getDescription() != null && !productSearchRequest.getDescription()
        .isEmpty()) {
      criteriaList.add(Criteria.where("description")
          .regex(".*" + productSearchRequest.getDescription() + ".*", "i"));
    }

    if (productSearchRequest.getMinPrice() != null) {
      criteriaList.add(Criteria.where("price").gte(productSearchRequest.getMinPrice()));
    }
    if (productSearchRequest.getMaxPrice() != null) {
      criteriaList.add(Criteria.where("price").lte(productSearchRequest.getMaxPrice()));
    }
    Query query = new Query(criteriaList.isEmpty() ?
        new Criteria() :
        new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
    return mongoTemplate.find(query, Product.class);
  }
}
