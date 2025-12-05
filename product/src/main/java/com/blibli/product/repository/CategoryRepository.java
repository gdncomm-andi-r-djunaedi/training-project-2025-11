package com.blibli.product.repository;

import com.blibli.product.entity.Category;
import com.blibli.product.entity.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {


}
