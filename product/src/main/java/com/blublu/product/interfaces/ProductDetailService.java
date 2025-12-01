package com.blublu.product.interfaces;

import com.blublu.product.document.ProductDetail;

public interface ProductDetailService {
  ProductDetail findProductDetailByName(String name);
}
