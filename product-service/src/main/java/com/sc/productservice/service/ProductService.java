package com.sc.productservice.service;

import com.sc.productservice.dto.ProductDto;
import com.sc.productservice.model.Product;
import com.sc.productservice.repository.ProductRepository;
import com.sc.utilsservice.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public ApiResponse<ProductDto> createProduct(ProductDto dto) {
        Optional<Product> existingProduct = productRepository.findByProductCode(dto.getProductCode());

        if (existingProduct.isPresent()) {
            return ApiResponse.failure(
                    "PRODUCT_ALREADY_EXISTS",
                    "Product already exists with code: " + dto.getProductCode()
            );
        }

        Product product = new Product();
        BeanUtils.copyProperties(dto, product);

        Product savedProduct = productRepository.save(product);

        ProductDto responseDto = new ProductDto();
        BeanUtils.copyProperties(savedProduct, responseDto);
        return ApiResponse.success(responseDto);
    }

    @Cacheable(key = "#productCode", value = "products")
    public ApiResponse<ProductDto> getProduct(String productCode) {
        Optional<Product> product = productRepository.findByProductCode(productCode);
        if (product.isPresent()) {
            ProductDto dto = new ProductDto();
            BeanUtils.copyProperties(product.get(), dto);
            return ApiResponse.success(dto);
        }
        return ApiResponse.failure("PRODUCT_NOT_FOUND", "Product not found with code: " + productCode);
    }

    public ApiResponse<Page<ProductDto>> getAllProducts(Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);
        return ApiResponse.success(products.map(product -> {
            ProductDto dto = new ProductDto();
            BeanUtils.copyProperties(product, dto);
            return dto;
        }));
    }

    public ApiResponse<Page<ProductDto>> searchProducts(String keyword, Pageable pageable) {
        Page<Product> products = productRepository.findByProductNameContainingIgnoreCase(keyword, pageable);

        return ApiResponse.success(products.map(product -> {
            ProductDto dto = new ProductDto();
            BeanUtils.copyProperties(product, dto);
            return dto;
        }));
    }

    @CachePut(key = "#p0.productCode", value = "products")
    public ApiResponse<ProductDto> updateProduct(ProductDto dto) {
        Optional<Product> existingProductOpt = productRepository.findByProductCode(dto.getProductCode());
        if (existingProductOpt.isPresent()) {
            Product existingProduct = existingProductOpt.get();
            BeanUtils.copyProperties(dto, existingProduct);
            Product updatedProduct = productRepository.save(existingProduct);
            ProductDto responseDto = new ProductDto();
            BeanUtils.copyProperties(updatedProduct, responseDto);
            return ApiResponse.success(responseDto);
        }

        return ApiResponse.failure("PRODUCT_NOT_FOUND", "Product not found with code: " + dto.getProductCode());
    }

    @CacheEvict(key = "productCode", value = "products")
    public ApiResponse<String> deleteProduct(String productCode) {

        Product product = productRepository.deleteProductByProductCode(productCode);
        if (product == null) {
            return ApiResponse.failure(
                    "PRODUCT_NOT_FOUND",
                    "No product found with code: " + productCode
            );
        }
        return ApiResponse.success("Product deleted successfully: " + productCode);
    }


}

