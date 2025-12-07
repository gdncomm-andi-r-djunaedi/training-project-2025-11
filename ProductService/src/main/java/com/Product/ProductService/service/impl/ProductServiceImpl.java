package com.Product.ProductService.service.impl;

import com.Product.ProductService.dto.ProductResponseDTO;
import com.Product.ProductService.entity.Product;
import com.Product.ProductService.exceptions.ProductServiceExceptions;
import com.Product.ProductService.repository.ProductRepository;
import com.Product.ProductService.service.ProductService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public ProductResponseDTO saveProduct(ProductResponseDTO productResponseDTO) {
        if (productResponseDTO == null) {
            throw new ProductServiceExceptions("Product body cannot be null", HttpStatus.BAD_REQUEST);
        }

        if (productResponseDTO.getProductName() == null || productResponseDTO.getProductName().trim().isEmpty()) {
            throw new ProductServiceExceptions("Product name cannot be empty", HttpStatus.BAD_REQUEST);
        }

        if (productResponseDTO.getProductDescription() == null || productResponseDTO.getProductDescription().trim().isEmpty()) {
            throw new ProductServiceExceptions("Product description cannot be empty", HttpStatus.BAD_REQUEST);
        }

        Product product = new Product();
        BeanUtils.copyProperties(productResponseDTO,product);
        return convertToDto(productRepository.save(product));
    }

    @Override
    public ProductResponseDTO getProductById(String id) {
        Product product =  productRepository.findById(id).orElseThrow(() -> new ProductServiceExceptions("Product not found with id " + id, HttpStatus.NOT_FOUND));
        return convertToDto(product);
    }

    @Override
    public Page<ProductResponseDTO> getProducts(Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);
        return products.map(this::convertToDto);
    }

    @Override
    public Page<ProductResponseDTO> searchProducts(@RequestParam(required = false) String keyword, Pageable pageable) {
        Page<Product> product;
        if (keyword != null && !keyword.trim().isEmpty()) {
            product = productRepository.findByProductNameContainingIgnoreCaseOrProductDescriptionContainingIgnoreCase(
                    keyword,keyword,
                    pageable
            );
        }

        else {
            product = productRepository.findAll(pageable);

        }

        return product.map(this::convertToDto);
    }

    private ProductResponseDTO convertToDto(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        BeanUtils.copyProperties(product, dto);
        return dto;
    }
}
