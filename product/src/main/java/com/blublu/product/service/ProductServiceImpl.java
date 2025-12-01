package com.blublu.product.service;

import com.blublu.product.document.Product;
import com.blublu.product.interfaces.ProductService;
import com.blublu.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
  @Autowired
  ProductRepository productRepository;

  @Override
  public List<Product> findAllProductWithPageAndSize(int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    return productRepository.findAll(pageable).getContent();
  }

  @Override
  public List<Product> findByName(String name, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    return productRepository.findProductByName(name, pageable);
  }
}
