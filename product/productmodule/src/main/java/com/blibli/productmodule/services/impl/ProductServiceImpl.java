package com.blibli.productmodule.services.impl;

import com.blibli.productmodule.dto.Productdto;
import com.blibli.productmodule.entity.ProductSearch;
import com.blibli.productmodule.repositories.ProductRepository;
import com.blibli.productmodule.services.ProductService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    ProductRepository productRepository;

    @Override
    public Page<Productdto> searchProducts(String searchTerm, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository
                .findByNameContainingIgnoreCase(searchTerm, pageable)
                .map(p -> convert(p));
    }

    @Override
    public Page<Productdto> listProduct(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository
                .findAll(pageable)
                .map(p -> convert(p));
    }

    @Override
    public Productdto productDetail(String productId) {
        ProductSearch productSearch = productRepository.findByProductCode(productId);
        if (productSearch == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Product with code '" + productId + "' not found"
            );
        }
        return convert(productSearch);
    }

    private Productdto convert(ProductSearch product) {
        Productdto productdto = new Productdto();
        productdto.setProductCode(product.getProductCode());
        productdto.setBrand(product.getBrand());
        productdto.setCategory(product.getCategory());
        productdto.setDescription(product.getDescription());
        productdto.setName(product.getName());
        productdto.setPrice(product.getPrice());
        productdto.setImage(product.getImageUrl());
        BeanUtils.copyProperties(product, productdto);
        return productdto;
    }
}
