package com.gdn.training.product.service;

import com.gdn.training.product.dto.ProductDetailResponse;
import com.gdn.training.product.dto.ProductListRequest;
import com.gdn.training.product.dto.SearchProductRequest;
import com.gdn.training.product.entity.Product;
import org.springframework.data.domain.Page;

public interface ProductService {

    ProductDetailResponse viewDetailById(String productId);

    Page<Product> viewProductList(ProductListRequest request);

    Page<Product> searchProduct(SearchProductRequest request);
}
