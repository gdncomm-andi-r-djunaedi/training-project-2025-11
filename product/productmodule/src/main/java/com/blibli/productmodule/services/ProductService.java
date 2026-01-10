package com.blibli.productmodule.services;

import com.blibli.productmodule.dto.Productdto;
import org.springframework.data.domain.Page;

public interface ProductService {
    Page<Productdto> searchProducts(String searchTerm, int page, int size);

    Page<Productdto> listProduct(int page, int size);

    Productdto productDetail(String productId);
}
