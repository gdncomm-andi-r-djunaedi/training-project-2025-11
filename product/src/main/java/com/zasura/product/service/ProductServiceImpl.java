package com.zasura.product.service;

import com.zasura.product.dto.ProductSearchRequest;
import com.zasura.product.entity.Product;
import com.zasura.product.exception.ProductNotFoundException;
import com.zasura.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
  private final ProductRepository productRepository;
  private final MongoTemplate mongoTemplate;

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
  public Page<Product> searchProducts(ProductSearchRequest productSearchRequest) {
    List<Criteria> criteriaList = new ArrayList<>();
    Pageable pageable = PageRequest.of(productSearchRequest.getPagination().getPage(),
        productSearchRequest.getPagination().getSize());
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
        new Criteria() : new Criteria().andOperator(criteriaList.toArray(new Criteria[0]))).with(
        pageable);
    List<Product> products = mongoTemplate.find(query, Product.class);

    return PageableExecutionUtils.getPage(products,
        pageable,
        () -> mongoTemplate.count(Query.of(query).skip(-1).limit(-1), Product.class));
  }

  @Override
  public Boolean migrate() {
    Product productRequest;
    for (int i = 0; i < 50000; i++) {
      productRequest =
          new Product("Product " + i, "Product Description " + i, Double.parseDouble("" + i + 1));
      productRepository.save(productRequest);
    }
    return true;
  }
}
