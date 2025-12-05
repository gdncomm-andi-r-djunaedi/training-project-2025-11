package org.edmund.product.services;

import org.edmund.product.dto.AddProductDto;
import org.edmund.product.response.AddProductResponse;
import org.edmund.product.response.GetProductListResponse;
import org.edmund.product.response.ProductDetailResponse;

public interface ProductService {
    AddProductResponse saveProduct(AddProductDto request);
    GetProductListResponse getListProduct(String name, Integer page, Integer size);
    ProductDetailResponse getProductDetail(String sku);
}
