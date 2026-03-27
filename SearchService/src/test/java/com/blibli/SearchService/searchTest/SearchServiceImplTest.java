package com.blibli.SearchService.searchTest;

import com.blibli.SearchService.entity.ProductDocument;
import com.blibli.SearchService.exception.InvalidSearchException;
import com.blibli.SearchService.exception.ProductNotFoundException;
import com.blibli.SearchService.service.impl.ProductSearchServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceImplTest {

    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    @Mock
    private SearchHits<ProductDocument> searchHits;

    @Mock
    private SearchHit<ProductDocument> searchHit;

    @InjectMocks
    private ProductSearchServiceImpl service;

    // ✅ SUCCESS CASE
    @Test
    void searchByName_success() {
        ProductDocument doc = new ProductDocument();
        doc.setSku("SKU-1");
        doc.setProductName("Laptop");

        when(searchHit.getContent()).thenReturn(doc);
        when(searchHits.getSearchHits()).thenReturn(List.of(searchHit));
        when(searchHits.getTotalHits()).thenReturn(1L);

        when(elasticsearchOperations.search(
                any(Query.class),
                eq(ProductDocument.class)
        )).thenReturn(searchHits);

        Page<ProductDocument> result = service.searchByName("Laptop", 0, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals("SKU-1", result.getContent().get(0).getSku());
    }


    @Test
    void searchByName_emptyName_shouldThrowInvalidSearchException() {
        InvalidSearchException ex = assertThrows(
                InvalidSearchException.class,
                () -> service.searchByName("", 0, 10)
        );

        assertEquals("Product name must not be empty", ex.getMessage());
    }


    @Test
    void searchByName_invalidPagination_shouldThrowInvalidSearchException() {
        InvalidSearchException ex = assertThrows(
                InvalidSearchException.class,
                () -> service.searchByName("Phone", -1, 0)
        );

        assertEquals("Invalid pagination parameters", ex.getMessage());
    }


    @Test
    void searchByName_noResults_shouldThrowProductNotFoundException() {
        when(searchHits.getTotalHits()).thenReturn(0L);

        when(elasticsearchOperations.search(
                any(Query.class),
                eq(ProductDocument.class)
        )).thenReturn(searchHits);

        ProductNotFoundException ex = assertThrows(
                ProductNotFoundException.class,
                () -> service.searchByName("Unknown", 0, 10)
        );

        assertEquals("No products found with name: Unknown", ex.getMessage());
    }


    @Test
    void searchBySku_success() {
        ProductDocument doc = new ProductDocument();
        doc.setSku("SKU-99");

        when(service.searchBySku("SKU-99")).thenReturn(doc);

        ProductDocument result = service.searchBySku("SKU-99");

        assertEquals("SKU-99", result.getSku());
    }

    @Test
    void searchBySku_emptySku_shouldThrowInvalidSearchException() {
        InvalidSearchException ex = assertThrows(
                InvalidSearchException.class,
                () -> service.searchBySku("")
        );

        assertEquals("SKU must not be empty", ex.getMessage());
    }

    @Test
    void searchBySku_notFound_shouldThrowProductNotFoundException() {
        when(service.searchBySku("SKU-X")).thenReturn(null);

        ProductNotFoundException ex = assertThrows(
                ProductNotFoundException.class,
                () -> service.searchBySku("SKU-X")
        );

        assertEquals("No product found for SKU: SKU-X", ex.getMessage());
    }
}
