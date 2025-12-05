package com.blibli.productModule.repository;

import com.blibli.productModule.entity.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Page<Product> searchProducts(String searchQuery, Pageable pageable) {
        log.info("Searching products with query: {}", searchQuery);
        Criteria searchCriteria = new Criteria().andOperator(
                new Criteria().orOperator(Criteria.where("name").regex(searchQuery, "i"),
                        Criteria.where("description").regex(searchQuery, "i")), Criteria.where("isActive").is(true));
        Query query = new Query(searchCriteria).with(pageable);
        List<Product> products = mongoTemplate.find(query, Product.class);
        Query countQuery = new Query(searchCriteria);
        long total = mongoTemplate.count(countQuery, Product.class);
        return new PageImpl<>(products, pageable, total);
    }

    @Override
    public Page<Product> searchProductsByCategory(String searchQuery, String category,
                                                  Pageable pageable) {
        log.info("Searching products with query: {} and category: {}", searchQuery, category);
        Criteria searchCriteria = new Criteria().andOperator(
                new Criteria().orOperator(Criteria.where("name").regex(searchQuery, "i"),
                        Criteria.where("description").regex(searchQuery, "i")),
                Criteria.where("category").is(category), Criteria.where("isActive").is(true));
        Query query = new Query(searchCriteria).with(pageable);
        List<Product> products = mongoTemplate.find(query, Product.class);
        Query countQuery = new Query(searchCriteria);
        long total = mongoTemplate.count(countQuery, Product.class);
        return new PageImpl<>(products, pageable, total);
    }
}

