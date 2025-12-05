package com.blibli.gdn.productService.service;

import com.blibli.gdn.productService.model.Product;
import com.blibli.gdn.productService.model.ProductDocument;
import com.blibli.gdn.productService.model.Variant;
import com.blibli.gdn.productService.model.VariantDocument;
import com.blibli.gdn.productService.repository.ProductDocumentRepository;
import com.blibli.gdn.productService.repository.ProductRepository;
import com.blibli.gdn.productService.service.impl.ProductIndexingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductIndexingServiceTest {

    @Mock
    private ProductDocumentRepository productDocumentRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    @Mock
    private IndexOperations indexOperations;

    @InjectMocks
    private ProductIndexingServiceImpl productIndexingService;

    private Product product;

    @BeforeEach
    void setUp() {
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
    }

    @Test
    void indexProduct_Success() {
        ProductDocument document = ProductDocument.builder()
                .id("P001")
                .productId("P001")
                .name("Test Product")
                .description("Test Description")
                .category("Electronics")
                .brand("Test Brand")
                .tags(Collections.singletonList("tag1"))
                .variants(Collections.singletonList(
                        VariantDocument.builder()
                                .sku("P001-BLACK-001")
                                .price(100.0)
                                .stock(10)
                                .color("Black")
                                .size("M")
                                .build()
                ))
                .build();

        when(productDocumentRepository.save(any(ProductDocument.class))).thenReturn(document);

        productIndexingService.indexProduct(product);

        verify(productDocumentRepository, times(1)).save(any(ProductDocument.class));
    }

    @Test
    void indexProduct_ErrorHandled() {
        when(productDocumentRepository.save(any(ProductDocument.class)))
                .thenThrow(new RuntimeException("Elasticsearch error"));

        // Should not throw exception
        assertDoesNotThrow(() -> productIndexingService.indexProduct(product));
        verify(productDocumentRepository, times(1)).save(any(ProductDocument.class));
    }

    @Test
    void updateProduct_Success() {
        ProductDocument document = ProductDocument.builder()
                .id("P001")
                .productId("P001")
                .name("Updated Product")
                .build();

        when(productDocumentRepository.save(any(ProductDocument.class))).thenReturn(document);

        productIndexingService.updateProduct(product);

        verify(productDocumentRepository, times(1)).save(any(ProductDocument.class));
    }

    @Test
    void deleteProduct_Success() {
        doNothing().when(productDocumentRepository).deleteById("P001");

        productIndexingService.deleteProduct("P001");

        verify(productDocumentRepository, times(1)).deleteById("P001");
    }

    @Test
    void deleteProduct_ErrorHandled() {
        doThrow(new RuntimeException("Elasticsearch error"))
                .when(productDocumentRepository).deleteById("P001");

        // Should not throw exception
        assertDoesNotThrow(() -> productIndexingService.deleteProduct("P001"));
        verify(productDocumentRepository, times(1)).deleteById("P001");
    }

    @Test
    void deleteProductById_Success() {
        when(productRepository.findById("mongo-id-1")).thenReturn(Optional.of(product));
        doNothing().when(productDocumentRepository).deleteById("P001");

        productIndexingService.deleteProductById("mongo-id-1");

        verify(productRepository, times(1)).findById("mongo-id-1");
        verify(productDocumentRepository, times(1)).deleteById("P001");
    }

    @Test
    void deleteProductById_ProductNotFound() {
        when(productRepository.findById("mongo-id-1")).thenReturn(Optional.empty());

        productIndexingService.deleteProductById("mongo-id-1");

        verify(productRepository, times(1)).findById("mongo-id-1");
        verify(productDocumentRepository, never()).deleteById(anyString());
    }

    @Test
    void reindexAllProducts_Success() {
        Page<Product> productPage = new PageImpl<>(Collections.singletonList(product));
        when(productRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(productPage);
        when(productDocumentRepository.saveAll(anyList())).thenReturn(Collections.emptyList());
        when(elasticsearchOperations.indexOps(ProductDocument.class)).thenReturn(indexOperations);
        when(indexOperations.exists()).thenReturn(true);
        when(indexOperations.delete()).thenReturn(true);
        when(indexOperations.create()).thenReturn(true);

        productIndexingService.reindexAllProducts();

        verify(productRepository, atLeastOnce()).findAll(any(org.springframework.data.domain.Pageable.class));
        verify(productDocumentRepository, atLeastOnce()).saveAll(anyList());
    }

    @Test
    void reindexAllProducts_IndexDoesNotExist() {
        Page<Product> productPage = new PageImpl<>(Collections.singletonList(product));
        when(productRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(productPage);
        when(productDocumentRepository.saveAll(anyList())).thenReturn(Collections.emptyList());
        when(elasticsearchOperations.indexOps(ProductDocument.class)).thenReturn(indexOperations);
        when(indexOperations.exists()).thenReturn(false);
        when(indexOperations.create()).thenReturn(true);

        productIndexingService.reindexAllProducts();

        verify(indexOperations, never()).delete();
        verify(indexOperations, times(1)).create();
        verify(productRepository, atLeastOnce()).findAll(any(org.springframework.data.domain.Pageable.class));
    }
}

