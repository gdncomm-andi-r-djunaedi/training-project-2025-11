package com.bootcamp.product.controller;

import com.bootcamp.product.model.Product;
import com.bootcamp.product.repository.ProductRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductController {
  private final ProductRepository productRepository;

  public ProductController(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  @RequestMapping(path = "/product", method = RequestMethod.GET)
  public Product getProduct(String sku) {
    return productRepository.findBySku(sku);
  }
}
