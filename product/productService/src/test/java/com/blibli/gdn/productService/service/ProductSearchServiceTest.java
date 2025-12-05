package com.blibli.gdn.productService.service;

import com.blibli.gdn.productService.mapper.ProductMapper;
import com.blibli.gdn.productService.model.Product;
import com.blibli.gdn.productService.model.ProductDocument;
import com.blibli.gdn.productService.model.Variant;
import com.blibli.gdn.productService.model.VariantDocument;
import com.blibli.gdn.productService.repository.ProductRepository;
import com.blibli.gdn.productService.service.impl.ProductSearchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductSearchServiceTest {

    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    @Mock
    private ProductRepository productRepository;

    @Spy
    private ProductMapper productMapper = new ProductMapper();

    @InjectMocks
    private ProductSearchServiceImpl productSearchService;

    private ProductDocument productDocument;
    private Product product;
    private SearchHits<ProductDocument> searchHits;
    
    // Helper method to setup searchHits when needed
    private void setupSearchHits() {
        @SuppressWarnings("unchecked")
        SearchHit<ProductDocument> searchHit = mock(SearchHit.class);
        when(searchHit.getContent()).thenReturn(productDocument);
        
        @SuppressWarnings("unchecked")
        SearchHits<ProductDocument> hits = mock(SearchHits.class);
        when(hits.getSearchHits()).thenReturn(Collections.singletonList(searchHit));
        when(hits.getTotalHits()).thenReturn(1L);
        searchHits = hits;
    }

    @BeforeEach
    void setUp() {
        productDocument = ProductDocument.builder()
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
                .build();

        // searchHits will be set up in individual tests that need it
    }

    @Test
    void searchProducts_ByName_Success() {
        setupSearchHits(); // Ensure searchHits is set up for this test
        when(elasticsearchOperations.search(any(CriteriaQuery.class), eq(ProductDocument.class)))
                .thenReturn(searchHits);
        when(productRepository.findFirstByProductId("P001")).thenReturn(Optional.of(product));

        var result = productSearchService.searchProducts("Test", null, PageRequest.of(0, 20), "name,asc");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Product", result.getContent().get(0).getName());
        verify(elasticsearchOperations, times(1)).search(any(CriteriaQuery.class), eq(ProductDocument.class));
        verify(productRepository, times(1)).findFirstByProductId("P001");
    }

    @Test
    void searchProducts_ByCategory_Success() {
        setupSearchHits();
        when(elasticsearchOperations.search(any(CriteriaQuery.class), eq(ProductDocument.class)))
                .thenReturn(searchHits);
        when(productRepository.findFirstByProductId("P001")).thenReturn(Optional.of(product));

        var result = productSearchService.searchProducts("", "Electronics", PageRequest.of(0, 20), "category,asc");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(elasticsearchOperations, times(1)).search(any(CriteriaQuery.class), eq(ProductDocument.class));
    }

    @Test
    void searchProducts_WithWildcard_Success() {
        setupSearchHits();
        when(elasticsearchOperations.search(any(CriteriaQuery.class), eq(ProductDocument.class)))
                .thenReturn(searchHits);
        when(productRepository.findFirstByProductId("P001")).thenReturn(Optional.of(product));

        var result = productSearchService.searchProducts("*Test*", null, PageRequest.of(0, 20), "name,asc");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(elasticsearchOperations, times(1)).search(any(CriteriaQuery.class), eq(ProductDocument.class));
    }

    @Test
    void searchProducts_MultiWord_Success() {
        setupSearchHits();
        when(elasticsearchOperations.search(any(CriteriaQuery.class), eq(ProductDocument.class)))
                .thenReturn(searchHits);
        when(productRepository.findFirstByProductId("P001")).thenReturn(Optional.of(product));

        var result = productSearchService.searchProducts("Test Product", null, PageRequest.of(0, 20), "name,asc");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(elasticsearchOperations, times(1)).search(any(CriteriaQuery.class), eq(ProductDocument.class));
    }

    @Test
    void searchProducts_EmptyCriteria_ReturnsAll() {
        @SuppressWarnings("unchecked")
        SearchHits<ProductDocument> emptyHits = mock(SearchHits.class);
        when(emptyHits.getSearchHits()).thenReturn(Collections.emptyList());
        when(emptyHits.getTotalHits()).thenReturn(0L);
        
        when(elasticsearchOperations.search(any(CriteriaQuery.class), eq(ProductDocument.class)))
                .thenReturn(emptyHits);

        var result = productSearchService.searchProducts("", null, PageRequest.of(0, 20), "name,asc");

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        verify(elasticsearchOperations, times(1)).search(any(CriteriaQuery.class), eq(ProductDocument.class));
    }

    @Test
    void searchProducts_ProductNotFoundInMongoDB() {
        setupSearchHits();
        when(elasticsearchOperations.search(any(CriteriaQuery.class), eq(ProductDocument.class)))
                .thenReturn(searchHits);
        when(productRepository.findFirstByProductId("P001")).thenReturn(Optional.empty());

        var result = productSearchService.searchProducts("Test", null, PageRequest.of(0, 20), "name,asc");

        assertNotNull(result);
        assertEquals(0, result.getContent().size()); // Product not found in MongoDB
        verify(productRepository, times(1)).findFirstByProductId("P001");
    }

    @Test
    void searchProducts_ElasticsearchError_ReturnsEmpty() {
        when(elasticsearchOperations.search(any(CriteriaQuery.class), eq(ProductDocument.class)))
                .thenThrow(new RuntimeException("Elasticsearch connection error"));

        var result = productSearchService.searchProducts("Test", null, PageRequest.of(0, 20), "name,asc");

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        verify(elasticsearchOperations, times(1)).search(any(CriteriaQuery.class), eq(ProductDocument.class));
    }

    @Test
    void searchProducts_WithSorting() {
        setupSearchHits();
        when(elasticsearchOperations.search(any(CriteriaQuery.class), eq(ProductDocument.class)))
                .thenReturn(searchHits);
        when(productRepository.findFirstByProductId("P001")).thenReturn(Optional.of(product));

        var result = productSearchService.searchProducts("Test", null, PageRequest.of(0, 20), "category,desc");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(elasticsearchOperations, times(1)).search(any(CriteriaQuery.class), eq(ProductDocument.class));
    }
}

