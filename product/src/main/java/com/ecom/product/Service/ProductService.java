package com.ecom.product.Service;

import com.ecom.product.Dto.ProductDto;
import com.ecom.product.Entitiy.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    Page<ProductDto> findAllByName(String search, int page, int size);

    ProductDto findBySku(String sku);

}
