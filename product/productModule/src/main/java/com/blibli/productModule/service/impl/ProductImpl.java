package com.blibli.productModule.service.impl;

import com.blibli.productModule.dto.ProductDto;
import com.blibli.productModule.dto.ProductSearchResponseDto;
import com.blibli.productModule.entity.Product;
import com.blibli.productModule.repository.ProductRepository;
import com.blibli.productModule.service.ProductService;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductImpl implements ProductService {

    @Autowired
    ProductRepository productRepository;

    @Override
    @CacheEvict(value = {"productSearch", "productList"}, allEntries = true)
    public ProductDto createProduct(ProductDto request) {
        log.info("Creating product with productId: {}", request.getProductId());

        if(productRepository.existsByProductId(request.getProductId())){
            throw  new RuntimeException("Product with productId " + request.getProductId() + " already exists");
        }

        Product product = new Product();
        product.setProductId(request.getProductId());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setBrand(request.getBrand());
        product.setAttributes(request.getAttributes());
        product.setImageUrl(request.getImageUrl());
        product.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        product.setCreatedAt(new Date());
        product.setUpdatedAt(new Date());

        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with id: {}", savedProduct.getId());
        return convertToResponse(savedProduct);
    }

    private ProductDto convertToResponse(Product product) {
        ProductDto productDto = new ProductDto();
        productDto.setProductId(product.getProductId());
        productDto.setName(product.getName());
        productDto.setDescription(product.getDescription());
        productDto.setCategory(product.getCategory());
        productDto.setPrice(product.getPrice());
        productDto.setBrand(product.getBrand());
        productDto.setAttributes(product.getAttributes());
        productDto.setImageUrl(product.getImageUrl());
        productDto.setIsActive(product.getIsActive());
        return productDto;
    }

    @Override
    @Cacheable(value = "productSearch", key = "(#searchTerm != null ? #searchTerm : '') + '_' + "
            + "(#category != null ? #category : 'all') + '_' + #page + '_' + #size")
    public ProductSearchResponseDto searchProducts(String searchTerm, String category, int page, int size){

        log.info("Searching products - query: {}, category: {}, page: {}, size: {}", searchTerm,
                category, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage;

        if (StringUtils.isNotEmpty(searchTerm)) {
            String searchQuery = searchTerm.trim();

            if (StringUtils.isNotEmpty(category)) {
                productPage =
                        productRepository.searchProductsByCategory(searchQuery, category.trim(), pageable);
            } else {
                productPage = productRepository.searchProducts(searchQuery, pageable);
            }

        }
        else {
            if (StringUtils.isNotEmpty(category)) {
                productPage = productRepository.findByCategoryAndIsActiveTrue(category.trim(), pageable);
            } else {
                productPage = productRepository.findByIsActiveTrue(pageable);
            }
        }

        List<ProductDto> content =
                productPage.getContent().stream().map(this::convertToResponse).collect(Collectors.toList());
        ProductSearchResponseDto productSearchResponseDto = new ProductSearchResponseDto();
        productSearchResponseDto.setContent(content);
        productSearchResponseDto.setPage(productPage.getNumber());
        productSearchResponseDto.setSize(productPage.getSize());
        productSearchResponseDto.setTotalElements(productPage.getTotalElements());
        productSearchResponseDto.setTotalPages(productPage.getTotalPages());
        log.info("Found {} products - cached with key: productSearch::{}",
                productPage.getTotalElements(), generateCacheKey(searchTerm, category, page, size));
        return productSearchResponseDto;
    }

    @Override
    @Cacheable(value = "productList", key = "(#category != null ? #category : 'all') + '_' + #page "
            + "+ '_' + #size")
    public ProductSearchResponseDto getAllProducts(String category, int page, int size) {
        log.info("Fetching all products - category: {}, page: {}, size: {}", category, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage;
        if (StringUtils.isNotEmpty(category)) {
            productPage = productRepository.findByCategoryAndIsActiveTrue(category.trim(), pageable);
        } else {
            productPage = productRepository.findByIsActiveTrue(pageable);
        }
        List<ProductDto> content =
                productPage.getContent().stream().map(this::convertToResponse).collect(Collectors.toList());
        ProductSearchResponseDto productSearchResponseDto = new ProductSearchResponseDto();
        productSearchResponseDto.setContent(content);
        productSearchResponseDto.setPage(productPage.getNumber());
        productSearchResponseDto.setSize(productPage.getSize());
        productSearchResponseDto.setTotalElements(productPage.getTotalElements());
        productSearchResponseDto.setTotalPages(productPage.getTotalPages());
        log.info("Products cached with key: productList::{}",
                generateCacheKey(null, category, page, size));
        return productSearchResponseDto;
    }

    @Override
    @Cacheable(value = "productDetail", key = "#productId")
    public ProductDto getProductById(String productId) {
        log.info("Fetching product with productId: {}", productId);
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with productId: " + productId));
        return convertToResponse(product);
    }

    @Override
    @CacheEvict(value = {"productDetail", "productSearch", "productList"}, allEntries = true)
    public void deleteProduct(String productId) {
        log.info("Deleting product with productId: {}", productId);
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with productId: " + productId));
        productRepository.delete(product);
        log.info("Product deleted successfully - productId: {}", productId);
        log.info("Cache evicted for productDetail, productSearch and productList after product deletion");
    }

    private String generateCacheKey(String searchTerm, String category, int page, int size) {
        String term = searchTerm != null ? searchTerm : "";
        String cat = category != null ? category : "all";
        return term + "_" + cat + "_" + page + "_" + size;
    }

}