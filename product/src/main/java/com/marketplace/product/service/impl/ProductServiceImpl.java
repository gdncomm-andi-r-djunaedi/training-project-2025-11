package com.marketplace.product.service.impl;

import com.marketplace.product.dto.ProductIdsRequest;
import com.marketplace.product.dto.ProductRequest;
import com.marketplace.product.dto.ProductResponse;
import com.marketplace.product.entity.Product;
import com.marketplace.product.exception.ProductNotFoundException;
import com.marketplace.product.repository.ProductRepository;
import com.marketplace.product.service.KafkaProducerService;
import com.marketplace.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final KafkaProducerService kafkaProducerService;

    @Override
    @CachePut(value = "products", key = "#result.productId")
    public ProductResponse createProduct(ProductRequest request) {
        String productId = generateUniqueProductId();
        
        Product product = Product.builder()
                .productId(productId)
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .imageUrl(request.getImageUrl())
                .category(request.getCategory())
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("Product created with productId: {}", productId);

        kafkaProducerService.publishProductCreated(savedProduct);

        ProductResponse response = new ProductResponse();
        BeanUtils.copyProperties(savedProduct, response);
        return response;
    }

    @Override
    @Cacheable(value = "products", key = "#productId")
    public ProductResponse getProduct(String productId) {
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        ProductResponse response = new ProductResponse();
        BeanUtils.copyProperties(product, response);
        return response;
    }

    @Override
    public List<ProductResponse> getProducts(ProductIdsRequest request) {
        List<Product> products = productRepository.findAll().stream()
                .filter(p -> request.getIds().contains(p.getProductId()))
                .collect(Collectors.toList());

        return products.stream()
                .map(product -> {
                    ProductResponse response = new ProductResponse();
                    BeanUtils.copyProperties(product, response);
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    @CachePut(value = "products", key = "#productId")
    public ProductResponse updateProduct(String productId, ProductRequest request) {
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setImageUrl(request.getImageUrl());
        product.setCategory(request.getCategory());

        Product updatedProduct = productRepository.save(product);
        log.info("Product updated with productId: {}", productId);

        kafkaProducerService.publishProductUpdated(updatedProduct);

        ProductResponse response = new ProductResponse();
        BeanUtils.copyProperties(updatedProduct, response);
        return response;
    }

    @Override
    @CacheEvict(value = "products", key = "#productId")
    public void deleteProduct(String productId) {
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        productRepository.delete(product);
        log.info("Product deleted with productId: {}", productId);

        kafkaProducerService.publishProductDeleted(productId);
    }

    @Override
    public int syncAllProductsToElasticsearch() {
        log.info("Starting sync of all products to Elasticsearch...");
        List<Product> allProducts = productRepository.findAll();

        int count = 0;
        for (Product product : allProducts) {
            kafkaProducerService.publishProductCreated(product);
            count++;

            if (count % 1000 == 0) {
                log.info("Synced {} products to Elasticsearch...", count);
            }
        }

        log.info("Sync completed! Total products synced: {}", count);
        return count;
    }

    private String generateUniqueProductId() {
        return "MTA-" + System.currentTimeMillis();
    }
}
