package com.blibli.gdn.productService.service;

import com.blibli.gdn.productService.dto.request.ProductRequest;
import com.blibli.gdn.productService.dto.request.VariantRequest;
import com.blibli.gdn.productService.dto.response.ProductResponse;
import com.blibli.gdn.productService.exception.ProductNotFoundException;
import com.blibli.gdn.productService.mapper.ProductMapper;
import com.blibli.gdn.productService.model.Product;
import com.blibli.gdn.productService.model.Variant;
import com.blibli.gdn.productService.repository.ProductRepository;
import com.blibli.gdn.productService.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductIndexingService productIndexingService;

    @Spy
    private ProductMapper productMapper = new ProductMapper();

    private ProductServiceImpl productService;

    private Product product;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        // Create service and inject mocks manually since ProductIndexingService uses @Autowired(required = false)
        productService = new ProductServiceImpl(productRepository, productMapper);
        // Use reflection to inject the optional ProductIndexingService
        try {
            java.lang.reflect.Field field = ProductServiceImpl.class.getDeclaredField("productIndexingService");
            field.setAccessible(true);
            field.set(productService, productIndexingService);
        } catch (Exception e) {
            // If reflection fails, continue without Elasticsearch service
        }
        
        product = Product.builder()
                .id("mongo-id-1")
                .productId("P001")
                .name("Test Product")
                .description("Test Description")
                .category("Electronics")
                .brand("Test Brand")
                .tags(Collections.singletonList("tag1"))
                .variants(Collections.singletonList(
                        Variant.builder()
                                .sku("P001-BLACK-001")
                                .price(100.0)
                                .stock(10)
                                .color("Black")
                                .size("M")
                                .build()
                ))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
                
        productRequest = ProductRequest.builder()
                .productId("P001")
                .name("Test Product")
                .description("Test Description")
                .category("Electronics")
                .brand("Test Brand")
                .tags(Collections.singletonList("tag1"))
                .variants(Collections.singletonList(
                        VariantRequest.builder()
                                .sku("P001-BLACK-001")
                                .price(100.0)
                                .stock(10)
                                .color("Black")
                                .size("M")
                                .build()
                ))
                .build();
    }

    @Test
    void createProduct_Success() {
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse created = productService.createProduct(productRequest);

        assertNotNull(created);
        assertEquals("Test Product", created.getName());
        assertEquals("P001", created.getProductId());
        verify(productRepository, times(1)).save(any(Product.class));
        verify(productIndexingService, times(1)).indexProduct(any(Product.class));
    }

    @Test
    void createProduct_WithoutElasticsearch() {
        // Create service without Elasticsearch
        ProductServiceImpl serviceWithoutES = new ProductServiceImpl(productRepository, productMapper);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse created = serviceWithoutES.createProduct(productRequest);

        assertNotNull(created);
        assertEquals("Test Product", created.getName());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void getProduct_Success() {
        when(productRepository.findFirstByProductId("P001")).thenReturn(Optional.of(product));

        ProductResponse found = productService.getProduct("P001");

        assertNotNull(found);
        assertEquals("mongo-id-1", found.getId());
        assertEquals("P001", found.getProductId());
        assertEquals("Test Product", found.getName());
    }

    @Test
    void getProduct_NotFound() {
        when(productRepository.findFirstByProductId("P001")).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.getProduct("P001"));
    }

    @Test
    void updateProduct_Success() {
        Product updatedProduct = Product.builder()
                .id("mongo-id-1")
                .productId("P001")
                .name("Updated Product")
                .description("Updated Description")
                .category("Electronics")
                .brand("Updated Brand")
                .tags(Collections.singletonList("tag1"))
                .variants(Collections.singletonList(
                        Variant.builder()
                                .sku("P001-BLACK-001")
                                .price(150.0)
                                .stock(20)
                                .color("Black")
                                .size("M")
                                .build()
                ))
                .createdAt(product.getCreatedAt())
                .updatedAt(Instant.now())
                .build();

        when(productRepository.findById("mongo-id-1")).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        ProductRequest updateRequest = ProductRequest.builder()
                .productId("P001")
                .name("Updated Product")
                .description("Updated Description")
                .category("Electronics")
                .brand("Updated Brand")
                .tags(Collections.singletonList("tag1"))
                .variants(Collections.singletonList(
                        VariantRequest.builder()
                                .sku("P001-BLACK-001")
                                .price(150.0)
                                .stock(20)
                                .color("Black")
                                .size("M")
                                .build()
                ))
                .build();

        ProductResponse updated = productService.updateProduct("mongo-id-1", updateRequest);

        assertNotNull(updated);
        assertEquals("Updated Product", updated.getName());
        assertEquals(150.0, updated.getVariants().get(0).getPrice());
        verify(productRepository, times(1)).findById("mongo-id-1");
        verify(productRepository, times(1)).save(any(Product.class));
        verify(productIndexingService, times(1)).updateProduct(any(Product.class));
    }

    @Test
    void updateProduct_NotFound() {
        when(productRepository.findById("mongo-id-1")).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, 
                () -> productService.updateProduct("mongo-id-1", productRequest));
    }

    @Test
    void deleteProduct_Success() {
        when(productRepository.findById("mongo-id-1")).thenReturn(Optional.of(product));
        doNothing().when(productRepository).deleteById("mongo-id-1");

        productService.deleteProduct("mongo-id-1");

        verify(productRepository, times(1)).findById("mongo-id-1");
        verify(productRepository, times(1)).deleteById("mongo-id-1");
        verify(productIndexingService, times(1)).deleteProduct("P001");
    }

    @Test
    void deleteProduct_NotFound() {
        when(productRepository.findById("mongo-id-1")).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, 
                () -> productService.deleteProduct("mongo-id-1"));
    }

    @Test
    void searchProducts_ByName() {
        Page<Product> page = new PageImpl<>(Collections.singletonList(product));
        when(productRepository.findByNameContainingIgnoreCase(anyString(), any(Pageable.class))).thenReturn(page);

        Page<ProductResponse> result = productService.searchProducts("Test", null, PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Product", result.getContent().get(0).getName());
    }

    @Test
    void searchProducts_ByNameAndCategory() {
        Page<Product> page = new PageImpl<>(Collections.singletonList(product));
        when(productRepository.findByNameContainingIgnoreCaseAndCategory(anyString(), anyString(), any(Pageable.class)))
                .thenReturn(page);

        Page<ProductResponse> result = productService.searchProducts("Test", "Electronics", PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository, times(1))
                .findByNameContainingIgnoreCaseAndCategory("Test", "Electronics", PageRequest.of(0, 10));
    }
}
