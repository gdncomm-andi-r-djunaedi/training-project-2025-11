package com.blibli.ProductService.service.impl;

import com.blibli.ProductService.dto.ProductDto;
import com.blibli.ProductService.entity.Product;
import com.blibli.ProductService.event.ProductEvent;
import com.blibli.ProductService.exception.ProductNotFoundException;
import com.blibli.ProductService.repository.ProductRepository;
import com.blibli.ProductService.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductEventPublisher productEventPublisher;

    public ProductServiceImpl(ProductRepository productRepository,ProductEventPublisher productEventPublisher) {
        this.productRepository = productRepository;
        this.productEventPublisher = productEventPublisher;
    }

    private static final String PRODUCT_CACHE = "products";


    @Override
    @Cacheable(value = PRODUCT_CACHE, key = "#productId")
    public ProductDto getProductById(String productId) {
        log.info("Getting Product by id {}", productId);
        Product p = productRepository.findById(productId).orElseThrow(()-> new ProductNotFoundException("Product not found with id: "+ productId));
        return convertToDto(p);
    }

    @Override
    @CachePut(value = PRODUCT_CACHE, key = "#result.sku")
    public ProductDto createProduct(ProductDto productDto) {
        log.info("Creating the Product: {}", productDto.getSku());

        if(productRepository.existsById(productDto.getSku())){
            throw new RuntimeException("Product already exists with Sku: "+ productDto.getSku());
        }

        Product p = new Product();
        BeanUtils.copyProperties(productDto,p);
        Product savedProduct = productRepository.save(p);
        productEventPublisher.sendProductCreated(savedProduct);
        return convertToDto(savedProduct);

    }

    @Override
    @CacheEvict(value = PRODUCT_CACHE, key = "#productId")
    public void deleteById(String productId) {
        log.info("deleting the Product: {}", productId);
        if (productRepository.existsById(productId)) {
            productRepository.deleteById(productId);
            log.info("deleted the Product: {}", productId);
        }
        else {
             throw new ProductNotFoundException("Product not there: "+ productId);
        }

    }

    @Override
    public int syncAllProductsToElasticsearch() {
        log.info("Starting sync of all products to Elasticsearch...");
        List<Product> allProducts = productRepository.findAll();

        int count = 0;
        for (Product product : allProducts) {
            productEventPublisher.sendProductCreated(product);
            count++;

            if (count % 1000 == 0) {
                log.info("Synced {} products to Elasticsearch...", count);
            }
        }

        log.info("Sync completed! Total products synced: {}", count);
        return count;
    }


    private Product toEntity(ProductDto dto) {
        return Product.builder()
                .sku(dto.getSku())
                .productName(dto.getProductName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .category(dto.getCategory())
                .build();
    }

    public ProductDto convertToDto(Product product){
        ProductDto p = new ProductDto();
        BeanUtils.copyProperties(product,p);
        return p;
    }
}
