package com.marketplace.search.service.impl;

import com.marketplace.search.document.ProductDocument;
import com.marketplace.search.dto.SearchResponse;
import com.marketplace.search.repository.ProductDocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceImplTest {

    @Mock
    private ProductDocumentRepository productDocumentRepository;

    @InjectMocks
    private SearchServiceImpl searchService;

    @Test
    void testSearch_Success() {
        String query = "laptop";
        int page = 0;
        int size = 10;

        ProductDocument document = new ProductDocument();
        document.setProductId("PROD-123");
        document.setTitle("Laptop");
        document.setPrice(BigDecimal.valueOf(1000));

        Page<ProductDocument> pageResult = new PageImpl<>(List.of(document), PageRequest.of(page, size), 1);

        when(productDocumentRepository.searchByTitleOrDescription(anyString(), any(Pageable.class)))
                .thenReturn(pageResult);

        SearchResponse response = searchService.search(query, page, size);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getContent().size());
        assertEquals("PROD-123", response.getContent().get(0).getProductId());
    }

    @Test
    void testSearch_NoResults() {
        String query = "nonexistent";
        Page<ProductDocument> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        when(productDocumentRepository.searchByTitleOrDescription(anyString(), any(Pageable.class)))
                .thenReturn(emptyPage);
        when(productDocumentRepository.existsById(anyString())).thenReturn(false);

        SearchResponse response = searchService.search(query, 0, 10);

        assertNotNull(response);
        assertEquals(0, response.getTotalElements());
        assertTrue(response.getContent().isEmpty());
    }
}

