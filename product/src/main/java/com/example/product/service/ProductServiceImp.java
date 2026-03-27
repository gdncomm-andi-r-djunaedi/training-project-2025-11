package com.example.product.service;

import com.example.product.dto.*;
import com.example.product.entity.ProductEntity;
import com.example.product.exception.BusinessException;
import com.example.product.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ProductServiceImp implements ProductService{
    
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private KafkaTemplate kafkaTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<ProductResponse> addProducts(List<ProductRequest> productRequests) {
        if (productRequests.isEmpty()) {
            throw new IllegalArgumentException("Product list cannot be null or empty");
        }
        List<ProductResponse> savedProducts = new ArrayList<>();
        List<ProductEntity> entitiesToSave = new ArrayList<>();
        List<String> itemSkusInRequest = new ArrayList<>();
        for (ProductRequest request : productRequests) {
            if (request == null) {
                log.warn("Product request cannot be null");
                continue;
            }
            if (StringUtils.isBlank(request.getItemSku())) {
                log.warn("ItemSku cannot be null or emptyString");
                continue;
            }
            if(StringUtils.isBlank(request.getProductName())){
                log.warn("productName cannot be null or emptyString");
                continue;
            }
            if(StringUtils.isBlank(request.getProductDescription())){
                log.warn("productDescription cannot be null or emptyString");
                continue;
            }
            if(request.getProductPrice()<=0 || request.getProductPrice()==null){
                log.warn("Product price cannot be negative or null");
                continue;
            }
            String itemSku = request.getItemSku().trim();
            if (itemSkusInRequest.contains(itemSku)) {
                log.warn(itemSku + " already exists in the same request");
                continue;
            }
            itemSkusInRequest.add(itemSku);
            if (productRepository.existsByItemSku(itemSku)) {
                log.warn(itemSku + " already exists in the collection");
                continue;
            }
            ProductEntity entity = convertToEntity(request);
            entitiesToSave.add(entity);
        }
        List<ProductEntity> savedEntities = productRepository.saveAll(entitiesToSave);
        for (ProductEntity entity : savedEntities) {
            savedProducts.add(convertToResponse(entity));
        }
        return savedProducts;
    }

    @Override
    public ProductListResponse getProductsListing(int pageNumber, int pageSize) {
        if (pageNumber < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (pageSize <= 0) {
            throw new IllegalArgumentException("Page size must be greater than 0");
        }
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<ProductEntity> productPage = productRepository.findAll(pageable);
        return buildProductListResponse(productPage);
    }

    @Override
    public ProductResponse getProductDetailByItemSku(String itemSku) {
        if (itemSku == null || itemSku.trim().isEmpty()) {
            throw new IllegalArgumentException("ItemSku cannot be null or empty");
        }
        ProductEntity productEntity = productRepository.findByItemSku(itemSku.trim()).orElse(null);
        if (productEntity==null) {
            throw new BusinessException("PRODUCT_NOT_FOUND", 
                "Product not found with ItemSku: " + itemSku);
        }
        return convertToResponse(productEntity);
    }
    
    @Override
    public ProductListResponse getProductsBySearchTerm(String searchTerm, int pageNumber, int pageSize) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new IllegalArgumentException("Search term cannot be null or empty");
        }
        if (pageNumber < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (pageSize <= 0) {
            throw new IllegalArgumentException("Page size must be greater than 0");
        }
        String trimmedTerm = searchTerm.trim();
        String regexPattern = buildWildcardRegex(trimmedTerm);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<ProductEntity> productPage = productRepository.findBySearchTerm(regexPattern, pageable);
        return buildProductListResponse(productPage);
    }

    @Override
    public String buildWildcardRegex(String searchTerm) {
        boolean hasWildcards = searchTerm.contains("*") || searchTerm.contains("?");
        StringBuilder regexBuilder = new StringBuilder();
        for (int i = 0; i < searchTerm.length(); i++) {
            char ch = searchTerm.charAt(i);
            if (ch == '*') {
                regexBuilder.append(".*");
            } else if (ch == '?') {
                regexBuilder.append(".");
            } else {
                if (ch == '.' || ch == '^' || ch == '$' || ch == '+' ||
                        ch == '|' || ch == '(' || ch == ')' || ch == '[' ||
                        ch == ']' || ch == '{' || ch == '}' || ch == '\\') {
                    regexBuilder.append("\\");
                }
                regexBuilder.append(ch);
            }
        }
        if (!hasWildcards) {
            return ".*" + regexBuilder.toString() + ".*";
        }
        return regexBuilder.toString();
    }

    @Override
    public ProductListResponse buildProductListResponse(Page<ProductEntity> productPage) {
        List<ProductResponse> productResponses = new ArrayList<>();
        for (ProductEntity entity : productPage.getContent()) {
            productResponses.add(convertToResponse(entity));
        }
        ProductListResponse response = new ProductListResponse();
        response.setProducts(productResponses);
        response.setCurrentPage(productPage.getNumber());
        response.setPageSize(productPage.getSize());
        response.setTotalElements(productPage.getTotalElements());
        response.setTotalPages(productPage.getTotalPages());
        response.setHasNext(productPage.hasNext());
        response.setHasPrevious(productPage.hasPrevious());
        return response;
    }

    @Override
    public ProductEntity convertToEntity(ProductRequest request) {
        ProductEntity entity = new ProductEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setItemSku(request.getItemSku().trim());
        entity.setProductName(request.getProductName());
        entity.setProductPrice(request.getProductPrice());
        entity.setProductDescription(request.getProductDescription());
        return entity;
    }

    @Override
    public ProductResponse convertToResponse(ProductEntity entity) {
        ProductResponse response = new ProductResponse();
        response.setItemSku(entity.getItemSku());
        response.setProductName(entity.getProductName());
        response.setProductPrice(entity.getProductPrice());
        response.setProductDescription(entity.getProductDescription());
        return response;
    }

    @Override
    public ProductResponse updateProduct(String itemSku, UpdateProductRequest updateRequest) throws Exception {
        if (itemSku == null || itemSku.trim().isEmpty()) {
            throw new IllegalArgumentException("ItemSku cannot be null or empty");
        }
        if (updateRequest == null) {
            throw new IllegalArgumentException("Update request cannot be null");
        }
        if(StringUtils.isBlank(updateRequest.getProductName())){
            throw new IllegalArgumentException("productName cannot be null or empty");
        }
        if(StringUtils.isBlank(updateRequest.getProductDescription())){
            throw new IllegalArgumentException("productDescription cannot be null or empty");
        }
        if(updateRequest.getProductPrice()<=0 || updateRequest.getProductPrice()==null){
            throw new IllegalArgumentException("productPrice cannot be negative or empty");
        }
        String trimmedItemSku = itemSku.trim();
        ProductEntity productEntity = productRepository.findByItemSku(trimmedItemSku).orElse(null);
        if (productEntity == null) {
            throw new BusinessException("PRODUCT_NOT_FOUND",
                    "Product not found with ItemSku: " + itemSku);
        }
        if (StringUtils.isNotEmpty(updateRequest.getProductName())) {
            productEntity.setProductName(updateRequest.getProductName());
        }
        if (updateRequest.getProductPrice() != null) {
            productEntity.setProductPrice(updateRequest.getProductPrice());
        }
        if (StringUtils.isNotEmpty(updateRequest.getProductDescription())) {
            productEntity.setProductDescription(updateRequest.getProductDescription());
        }

        ProductEntity updatedEntity = productRepository.save(productEntity);
        ProductUpdateEvent productUpdateEvent = new ProductUpdateEvent();
        productUpdateEvent.setProductId(updatedEntity.getItemSku());
        productUpdateEvent.setProductName(updatedEntity.getProductName());
        productUpdateEvent.setItemPrice(updatedEntity.getProductPrice());
        kafkaTemplate.send("product.update.event",objectMapper.writeValueAsString(productUpdateEvent));
        return convertToResponse(updatedEntity);
    }

    @Override
    public void deleteProductByItemSku(String itemSku) throws Exception {
        if (itemSku == null || itemSku.trim().isEmpty()) {
            throw new IllegalArgumentException("ItemSku cannot be null or empty");
        }
        ProductEntity productEntity = productRepository.findByItemSku(itemSku.trim()).orElse(null);
        if (productEntity==null) {
            throw new BusinessException("PRODUCT_NOT_FOUND",
                    "Product not found with ItemSku: " + itemSku);
        }
        productRepository.deleteByItemSku(itemSku);
        ProductDeleteEvent productDeleteEvent = new ProductDeleteEvent();
        productDeleteEvent.setProductId(itemSku);
        kafkaTemplate.send("product.delete.event",objectMapper.writeValueAsString(productDeleteEvent));
    }

}

