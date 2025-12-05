package com.gdn.product.service;

import com.gdn.product.dto.request.ProductDTO;
import com.gdn.product.dto.response.ProductSearchResponseDTO;
import com.gdn.product.dto.request.SearchProductDTO;

public interface ProductService {

    public ProductDTO createProduct(ProductDTO productDTO);
    public ProductDTO update(ProductDTO productDTO);
    public ProductSearchResponseDTO search(SearchProductDTO searchProductDTO);
    ProductDTO getProductDetail(String productId);
}
