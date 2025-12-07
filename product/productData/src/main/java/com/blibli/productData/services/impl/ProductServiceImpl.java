package com.blibli.productData.services.impl;

import com.blibli.productData.dto.ProductDTO;
import com.blibli.productData.entity.Product;
import com.blibli.productData.repositories.ProductRepository;
import com.blibli.productData.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;


@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    ProductRepository productRepository;

    @Override
    public Page<ProductDTO> getAllProductList(Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);
        if (products == null) {
            products = Page.empty();
        }
        return products.map(product -> methodToDto(product));
    }

    @Override
    public Page<ProductDTO> queryProducts(String searchTerm, Pageable pageable) {
        String escaped = Pattern.quote(searchTerm);

        String pattern = ".*" + escaped + ".*";

        Page<Product> products = productRepository.searchProducts(pattern, pageable);
        if (products == null) {
            products = Page.empty();
        }
        return products.map(product -> methodToDto(product));

    }

    @Override
    public ProductDTO getProductDetail(String productId) {
        Product product = productRepository.findByProductId(productId);
        if (product == null) {
            return null;
        }
        return methodToDto(product);
    }

    public ProductDTO methodToDto(Product product) {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setProductId(product.getProductId());
        productDTO.setName(product.getName());
        productDTO.setDescription(product.getDescription());
        productDTO.setPrice(product.getPrice());
        productDTO.setBrand(product.getBrand());
        productDTO.setImageUrl(product.getImageUrl());
        productDTO.setCategories(product.getCategories());
        return productDTO;
    }

}
