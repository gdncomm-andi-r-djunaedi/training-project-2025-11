package com.gdn.training.product.service;

import com.gdn.training.product.dto.ProductListRequest;
import com.gdn.training.product.dto.SearchProductRequest;
import com.gdn.training.product.entity.Product;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface ProductService {

    Optional<Product> viewDetailById (String product_id);
    Page<Product> viewProductList(ProductListRequest request);
    Page<Product> searchProduct(SearchProductRequest request);
}
