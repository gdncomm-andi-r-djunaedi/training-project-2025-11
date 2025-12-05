package com.blublu.product.interfaces;

import com.blublu.product.document.Products;

import java.util.List;

public interface ProductService {
  List<Products> findAllProductWithPageAndSize(int page, int size);

  List<Products> findByName(String name, int page, int size);

  Products findProductBySkuCode(String name);

}
