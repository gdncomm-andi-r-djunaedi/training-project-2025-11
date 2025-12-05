package com.blublu.product.service;

import com.blublu.product.document.Products;
import com.blublu.product.interfaces.ProductService;
import com.blublu.product.repository.ProductsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
  @Autowired
  ProductsRepository productsRepository;

  @Override
  public List<Products> findAllProductWithPageAndSize(int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    return productsRepository.findAll(pageable).getContent();
  }

  @Override
  public List<Products> findByName(String name, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    return productsRepository.findProductsByName(name, pageable);
  }

  @Override
  public Products findProductBySkuCode(String skuCode) {
    return productsRepository.findBySkuCode(skuCode);
  }
}
