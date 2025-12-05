package com.microservice.product.service.serviceImpl;

// Remove this import
// import com.fasterxml.jackson.databind.ObjectMapper;

import com.microservice.product.dto.ProductDto;
import com.microservice.product.dto.ProductResponseDto;
import com.microservice.product.entity.Product;
import com.microservice.product.exception.ResourceNotFoundException;
import com.microservice.product.exception.ValidationException;
import com.microservice.product.repository.ProductRepository;
import com.microservice.product.service.ProductEventPublisher;
import com.microservice.product.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductEventPublisher productEventPublisher;

    @Override
    @Transactional
    public Page<ProductResponseDto> getProducts(Pageable pageable) {
        Page<Product> productEntities = productRepository.findAll(pageable);
        log.info("Retrieved {} products from database (total: {})",
                productEntities.getNumberOfElements(), productEntities.getTotalElements());
        return productEntities.map(productEntity -> convertToDto(productEntity));
    }

    @Override
    @Transactional
    @Cacheable(value = "products", key = "'search:' + #searchTerm + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    public Page<ProductResponseDto> getProductsBySearch(String searchTerm, Pageable pageable) {
        Page<Product> productEntities = productRepository.findBySearchTerm(searchTerm, pageable);
        log.info("Found {} products matching search term '{}' (total: {})",
                productEntities.getNumberOfElements(), searchTerm, productEntities.getTotalElements());
        return productEntities.map(productEntity -> convertToDto(productEntity));
    }

    @Override
    @Transactional
    @Cacheable(value = "product", key = "#skuId")
    public ProductResponseDto getProductsById(String skuId) {
        if(!productRepository.existsBySkuId(skuId)){
            log.warn("Product with SKU ID {} not found", skuId);
            throw new ResourceNotFoundException("Product", skuId);
        }
        Product product = productRepository.findBySkuId(skuId)
                .orElseThrow(() -> new ResourceNotFoundException("Product was not found"));
        log.info("Successfully retrieved product with SKU ID: {}", skuId);
        return convertToDto(product);
    }

    @Override
    @Transactional
    public ProductResponseDto addProduct(ProductDto productDto) {
        Product product = convertToEntity(productDto);

        Product savedProduct = productRepository.save(product);
        log.info("Successfully added product with ID: {}, SKU: {}",
                savedProduct.getId(), savedProduct.getSkuId());
        ProductResponseDto responseDto = convertToDto(savedProduct);
        try {
            productEventPublisher.publishProductCreated(responseDto);
        } catch (Exception e) {
            log.error("Failed to publish product created event for SKU: {}", savedProduct.getSkuId(), e);
        }
        return responseDto;
    }

    @Override
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "product", key = "#skuId", condition = "#skuId != null && #productDto != null"),
                    @CacheEvict(value = "products", allEntries = true, condition = "#skuId != null")
            },
            put = {
                    @CachePut(value = "product", key = "#skuId", condition = "#skuId != null && #productDto != null")
            }
    )
    public ProductResponseDto updateProduct(String skuId, ProductDto productDto) {
        Product product = productRepository.findBySkuId(skuId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", skuId));

        product.setSkuId(productDto.getSkuId());
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setCategory(productDto.getCategory());
        product.setBrand(productDto.getBrand());
        product.setPrice(productDto.getPrice());
        product.setItemCode(productDto.getItemCode());
        product.setLength(productDto.getLength());
        product.setHeight(productDto.getHeight());
        product.setWidth(productDto.getWidth());
        product.setWeight(productDto.getWeight());
        product.setDangerousLevel(productDto.getDangerousLevel());
        product.setUpdatedAt(java.time.LocalDateTime.now());

        Product updatedProduct = productRepository.save(product);
        log.info("Successfully updated product with SKU ID: {}", updatedProduct.getSkuId());
        
        ProductResponseDto responseDto = convertToDto(updatedProduct);
        try {
            productEventPublisher.publishProductUpdated(responseDto);
        } catch (Exception e) {
            log.error("Failed to publish product updated event for SKU: {}", updatedProduct.getSkuId(), e);
        }
        return responseDto;
    }

    @Override
    @Transactional
    @CacheEvict(value = "product", key = "#skuId")
    public void deleteById(String skuId) {
        log.info("Deleting product with SKU ID: {}", skuId);

        // Check if product exists before attempting to delete
        if (!productRepository.existsBySkuId(skuId)) {
            log.warn("Attempted to delete non-existent product with SKU ID: {}", skuId);
            throw new ResourceNotFoundException("Product", skuId);
        }

        productRepository.deleteBySkuId(skuId);
        log.info("Successfully deleted product with SKU ID: {}", skuId);
        try {
            productEventPublisher.publishProductDeleted(skuId);
        } catch (Exception e) {
            log.error("Failed to publish product deleted event for SKU: {}", skuId, e);
        }
    }

    @Override
    @Transactional
    @Cacheable(value = "product", key = "#skuId")
    public Boolean isProductIdPresent(String skuId) {
        Boolean exists = productRepository.existsBySkuId(skuId);
        log.debug("Product with SKU ID {} exists: {}", skuId, exists);
        return exists;
    }

    @Override
    @Transactional
    public List<ProductResponseDto> getProductsBySkuIds(List<String> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) {
            log.error("SKU IDs list is null or empty");
            throw new ValidationException("SKU IDs list cannot be null or empty");
        }

        List<Product> products = productRepository.findBySkuIdIn(skuIds);
        log.info("Found {} products for {} SKU IDs", products.size(), skuIds.size());

        if (products.isEmpty()) {
            log.warn("No products found for the given SKU IDs: {}", skuIds);
            throw new ResourceNotFoundException("No products found for the given SKU IDs");
        }

        List<ProductResponseDto> result = products.stream()
                .map(product -> convertToDto(product))
                .collect(Collectors.toList());

        log.info("Successfully retrieved {} products by SKU IDs", result.size());
        return result;
    }

    private ProductResponseDto convertToDto(Product productEntity) {
        if (productEntity == null) {
            return null;
        }

        ProductResponseDto dto = new ProductResponseDto();
        dto.setSkuId(productEntity.getSkuId());
        dto.setStoreId(productEntity.getStoreId());
        dto.setName(productEntity.getName());
        dto.setDescription(productEntity.getDescription());
        dto.setCategory(productEntity.getCategory());
        dto.setBrand(productEntity.getBrand());
        dto.setPrice(productEntity.getPrice());
        dto.setItemCode(productEntity.getItemCode());
        dto.setIsActive(productEntity.getIsActive());
        dto.setLength(productEntity.getLength());
        dto.setHeight(productEntity.getHeight());
        dto.setWidth(productEntity.getWidth());
        dto.setWeight(productEntity.getWeight());
        dto.setDangerousLevel(productEntity.getDangerousLevel());
        dto.setCreatedAt(productEntity.getCreatedAt());
        dto.setUpdatedAt(productEntity.getUpdatedAt());

        return dto;
    }

    private Product convertToEntity(ProductDto productDto) {
        if (productDto == null) {
            return null;
        }

        Product product = new Product();
        product.setSkuId(productDto.getSkuId());
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setCategory(productDto.getCategory());
        product.setBrand(productDto.getBrand());
        product.setPrice(productDto.getPrice());
        product.setItemCode(productDto.getItemCode());
        product.setLength(productDto.getLength());
        product.setHeight(productDto.getHeight());
        product.setWidth(productDto.getWidth());
        product.setWeight(productDto.getWeight());
        product.setDangerousLevel(productDto.getDangerousLevel());

        return product;
    }
}