package com.blibli.gdn.productService.service.impl;

import com.blibli.gdn.productService.dto.request.ProductRequest;
import com.blibli.gdn.productService.dto.response.ProductResponse;
import com.blibli.gdn.productService.exception.ProductNotFoundException;
import com.blibli.gdn.productService.mapper.ProductMapper;
import com.blibli.gdn.productService.model.Product;
import com.blibli.gdn.productService.repository.ProductRepository;
import com.blibli.gdn.productService.service.ProductIndexingService;
import com.blibli.gdn.productService.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    
    @Autowired(required = false)
    private ProductIndexingService productIndexingService;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest productRequest) {
        log.info("Creating product: {}", productRequest.getName());
        Product product = productMapper.toProduct(productRequest);
        Product savedProduct = productRepository.save(product);
        
        // Index product in Elasticsearch asynchronously (if Elasticsearch is available)
        if (productIndexingService != null) {
            productIndexingService.indexProduct(savedProduct);
        }
        
        return productMapper.toProductResponse(savedProduct);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(String id, ProductRequest productRequest) {
        log.info("Updating product with id: {}", id);
        
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        
        // Update product fields
        Product updatedProduct = productMapper.toProduct(productRequest);
        updatedProduct.setId(existingProduct.getId()); // Preserve ID
        updatedProduct.setCreatedAt(existingProduct.getCreatedAt()); // Preserve creation date
        updatedProduct.setUpdatedAt(java.time.Instant.now()); // Update timestamp
        
        Product savedProduct = productRepository.save(updatedProduct);
        
        // Update product in Elasticsearch asynchronously
        if (productIndexingService != null) {
            productIndexingService.updateProduct(savedProduct);
        }
        
        return productMapper.toProductResponse(savedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(String id) {
        log.info("Deleting product with id: {}", id);
        
        // Get product first to get productId for Elasticsearch deletion
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        
        String productId = product.getProductId();
        
        // Delete from MongoDB
        productRepository.deleteById(id);
        
        // Delete from Elasticsearch asynchronously using productId
        if (productIndexingService != null) {
            productIndexingService.deleteProduct(productId);
        }
        
        log.info("Successfully deleted product: {} (productId: {})", id, productId);
    }

    @Override
    @Cacheable(value = "products", key = "#id")
    @Transactional(readOnly = true)
    public ProductResponse getProduct(String id) {
        log.info("Fetching product with productId: {}", id);
        
        // Use findFirstByProductId to handle duplicates gracefully
        Product product = productRepository.findFirstByProductId(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with productId: " + id));
        
        return productMapper.toProductResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String name, String category, Pageable pageable) {
        log.info("Searching products with name: {}, category: {}", name, category);
        Page<Product> products;
        if (category != null && !category.isEmpty()) {
            products = productRepository.findByNameContainingIgnoreCaseAndCategory(name, category, pageable);
        } else {
            products = productRepository.findByNameContainingIgnoreCase(name, pageable);
        }
        return products.map(productMapper::toProductResponse);
    }
}
