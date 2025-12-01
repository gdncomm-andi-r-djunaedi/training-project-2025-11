package com.blublu.product.interfaces;

import com.blublu.product.document.Product;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ProductService {
  List<Product> findAllProductWithPageAndSize(int page, int size);

  List<Product> findByName(String name, int page, int size);
}
