package com.kailash.product.service;

import com.kailash.product.entity.Product;
import com.kailash.product.entity.ProductIndex;

public interface ProductIndexService {

    void index(ProductIndex productIndex);

    Iterable<ProductIndex> search(String keyword);
    void reindexAll();
}
