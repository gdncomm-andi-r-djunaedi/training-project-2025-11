package com.blublu.product.service;

import com.blublu.product.document.ProductDetail;
import com.blublu.product.interfaces.ProductDetailService;
import com.blublu.product.repository.ProductDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductDetailServiceImpl implements ProductDetailService {
  @Autowired
  ProductDetailRepository productDetailRepository;

  @Override
  public ProductDetail findProductDetailByName(String name) {
    return productDetailRepository.findProductByName(name);
  }
}
