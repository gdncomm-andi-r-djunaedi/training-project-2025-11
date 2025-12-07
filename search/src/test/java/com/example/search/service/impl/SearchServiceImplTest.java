package com.example.search.service.impl;

import com.example.search.dto.ProductDocumentDto;
import com.example.search.dto.SearchResponse;
import com.example.search.entity.ProductDocument;
import com.example.search.exceptions.SearchException;
import com.example.search.repository.ProductRepository;
import com.example.search.utils.PaginationMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private SearchServiceImpl searchService;

    private Pageable pageable;
    private ProductDocument productDocument1;
    private ProductDocument productDocument2;
    private Page<ProductDocument> productPage;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);

        productDocument1 = new ProductDocument();
        productDocument1.setId("1");
        productDocument1.setProductId(1L);
        productDocument1.setTitle("Laptop Computer");
        productDocument1.setDescription("High performance laptop");
        productDocument1.setPrice(999.99);
        productDocument1.setImageUrl("laptop.jpg");
        productDocument1.setCategory("Electronics");
        productDocument1.setMarkForDelete(false);

        productDocument2 = new ProductDocument();
        productDocument2.setId("2");
        productDocument2.setProductId(2L);
        productDocument2.setTitle("Wireless Mouse");
        productDocument2.setDescription("Ergonomic wireless mouse");
        productDocument2.setPrice(29.99);
        productDocument2.setImageUrl("mouse.jpg");
        productDocument2.setCategory("Electronics");
        productDocument2.setMarkForDelete(false);

        List<ProductDocument> products = Arrays.asList(productDocument1, productDocument2);
        productPage = new PageImpl<>(products, pageable, products.size());
    }

    @Test
    void search_validTerm_returnsSearchResults() {

        String searchTerm = "laptop";
        when(productRepository.wildcardSearch(searchTerm, pageable)).thenReturn(productPage);

        SearchResponse result = searchService.search(searchTerm, pageable);

        assertNotNull(result);
        assertNotNull(result.getProducts());
        assertEquals(2, result.getProducts().size());
        assertEquals("Laptop Computer", result.getProducts().get(0).getTitle());
        assertNotNull(result.getMetadata());
        assertEquals(0, result.getMetadata().getPage());
        assertEquals(10, result.getMetadata().getSize());

        verify(productRepository).wildcardSearch(searchTerm, pageable);
    }

    @Test
    void searchWithPriority_validTerm_returnsSearchResults() {

        String searchTerm = "mouse";
        when(productRepository.wildcardSearchWithPriority(searchTerm, pageable)).thenReturn(productPage);

        SearchResponse result = searchService.searchWithPriority(searchTerm, pageable);

        assertNotNull(result);
        assertNotNull(result.getProducts());
        assertEquals(2, result.getProducts().size());
        assertNotNull(result.getMetadata());

        verify(productRepository).wildcardSearchWithPriority(searchTerm, pageable);
    }

    @Test
    void searchWithPriorityAndPriceSort_ascOrder_returnsSortedResults() {

        String searchTerm = "electronics";
        String sortOrder = "asc";
        when(productRepository.wildcardSearchWithPriorityAndPriceSort(searchTerm, pageable, sortOrder))
                .thenReturn(productPage);

        SearchResponse result = searchService.searchWithPriorityAndPriceSort(searchTerm, pageable, sortOrder);

        assertNotNull(result);
        assertNotNull(result.getProducts());
        assertEquals(2, result.getProducts().size());

        verify(productRepository).wildcardSearchWithPriorityAndPriceSort(searchTerm, pageable, sortOrder);
    }

    @Test
    void searchWithPriorityAndPriceSort_descOrder_returnsSortedResults() {

        String searchTerm = "electronics";
        String sortOrder = "desc";
        when(productRepository.wildcardSearchWithPriorityAndPriceSort(searchTerm, pageable, sortOrder))
                .thenReturn(productPage);

        SearchResponse result = searchService.searchWithPriorityAndPriceSort(searchTerm, pageable, sortOrder);

        assertNotNull(result);
        assertNotNull(result.getProducts());
        assertEquals(2, result.getProducts().size());

        verify(productRepository).wildcardSearchWithPriorityAndPriceSort(searchTerm, pageable, sortOrder);
    }

    @Test
    void search_noResults_returnsEmptyResponse() {

        String searchTerm = "nonexistent";
        Page<ProductDocument> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(productRepository.wildcardSearch(searchTerm, pageable)).thenReturn(emptyPage);

        SearchResponse result = searchService.search(searchTerm, pageable);

        assertNotNull(result);
        assertNotNull(result.getProducts());
        assertTrue(result.getProducts().isEmpty());
        assertEquals(0, result.getMetadata().getTotalItems());
    }

    @Test
    void search_withPagination_returnsCorrectPage() {

        String searchTerm = "laptop";
        Pageable secondPage = PageRequest.of(1, 10);
        Page<ProductDocument> page = new PageImpl<>(
                Collections.singletonList(productDocument1),
                secondPage,
                15
        );
        when(productRepository.wildcardSearch(searchTerm, secondPage)).thenReturn(page);

        SearchResponse result = searchService.search(searchTerm, secondPage);

        assertNotNull(result);
        assertEquals(1, result.getMetadata().getPage());
        assertEquals(10, result.getMetadata().getSize());
        assertEquals(1, result.getMetadata().getTotalItems());
        assertEquals(2, result.getMetadata().getTotalPage());
    }

    @Test
    void searchWithPriorityAndPriceSort_nullSortOrder_handlesGracefully() {

        String searchTerm = "laptop";
        when(productRepository.wildcardSearchWithPriorityAndPriceSort(searchTerm, pageable, null))
                .thenReturn(productPage);

        SearchResponse result = searchService.searchWithPriorityAndPriceSort(searchTerm, pageable, null);

        assertNotNull(result);
        assertEquals(2, result.getProducts().size());
    }

    @Test
    void search_withNullPrice_handlesGracefully() {

        String searchTerm = "product";
        productDocument1.setPrice(null);
        List<ProductDocument> products = Collections.singletonList(productDocument1);
        Page<ProductDocument> page = new PageImpl<>(products, pageable, 1);

        when(productRepository.wildcardSearch(searchTerm, pageable)).thenReturn(page);

        SearchResponse result = searchService.search(searchTerm, pageable);

        assertNotNull(result);
        assertEquals(1, result.getProducts().size());
        assertNull(result.getProducts().get(0).getPrice());
    }

    @Test
    void search_nullTerm_throwsSearchException() {

        SearchException exception = assertThrows(
                SearchException.class,
                () -> searchService.search(null, pageable)
        );

        assertEquals("Search term cannot be null or empty", exception.getMessage());
        verify(productRepository, never()).wildcardSearch(anyString(), any(Pageable.class));
    }

    @Test
    void search_emptyTerm_throwsSearchException() {

        SearchException exception = assertThrows(
                SearchException.class,
                () -> searchService.search("", pageable)
        );

        assertEquals("Search term cannot be null or empty", exception.getMessage());
        verify(productRepository, never()).wildcardSearch(anyString(), any(Pageable.class));
    }

    @Test
    void search_singleCharacterTerm_throwsSearchException() {

        SearchException exception = assertThrows(
                SearchException.class,
                () -> searchService.search("a", pageable)
        );

        assertEquals("Search term must be at least 2 characters long", exception.getMessage());
        verify(productRepository, never()).wildcardSearch(anyString(), any(Pageable.class));
    }

    @Test
    void searchWithPriorityAndPriceSort_invalidSortOrder_throwsSearchException() {

        String searchTerm = "laptop";
        String invalidSortOrder = "invalid";

        SearchException exception = assertThrows(
                SearchException.class,
                () -> searchService.searchWithPriorityAndPriceSort(searchTerm, pageable, invalidSortOrder)
        );

        assertEquals("Sort order must be either 'asc' or 'desc'", exception.getMessage());
        verify(productRepository, never()).wildcardSearchWithPriorityAndPriceSort(anyString(), any(), anyString());
    }

    @Test
    void search_repositoryFailure_throwsSearchException() {

        String searchTerm = "laptop";
        when(productRepository.wildcardSearch(searchTerm, pageable))
                .thenThrow(new RuntimeException("Solr connection error"));

        SearchException exception = assertThrows(
                SearchException.class,
                () -> searchService.search(searchTerm, pageable)
        );

        assertTrue(exception.getMessage().contains("Failed to perform search"));
        verify(productRepository).wildcardSearch(searchTerm, pageable);
    }

    @Test
    void searchWithPriority_repositoryFailure_throwsSearchException() {

        String searchTerm = "laptop";
        when(productRepository.wildcardSearchWithPriority(searchTerm, pageable))
                .thenThrow(new RuntimeException("Solr error"));

        SearchException exception = assertThrows(
                SearchException.class,
                () -> searchService.searchWithPriority(searchTerm, pageable)
        );

        assertTrue(exception.getMessage().contains("Failed to perform priority search"));
    }

    @Test
    void searchWithPriorityAndPriceSort_repositoryFailure_throwsSearchException() {

        String searchTerm = "laptop";
        String sortOrder = "asc";
        when(productRepository.wildcardSearchWithPriorityAndPriceSort(searchTerm, pageable, sortOrder))
                .thenThrow(new RuntimeException("Solr error"));

        SearchException exception = assertThrows(
                SearchException.class,
                () -> searchService.searchWithPriorityAndPriceSort(searchTerm, pageable, sortOrder)
        );

        assertTrue(exception.getMessage().contains("Failed to perform priority search with price sort"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "  ", "a"})
    void validateSearchTerm_variousInvalidTerms_throwsException(String invalidTerm) {

        assertThrows(
                SearchException.class,
                () -> searchService.search(invalidTerm, pageable)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid", "ASC", "DESC", "random", "ascending", "descending"})
    void validateSortOrder_variousInvalidOrders_throwsException(String invalidOrder) {

        String searchTerm = "laptop";
        assertThrows(
                SearchException.class,
                () -> searchService.searchWithPriorityAndPriceSort(searchTerm, pageable, invalidOrder)
        );
    }

    @ParameterizedTest
    @CsvSource({
            "10, 0, 10",
            "20, 0, 20",
            "50, 0, 50",
            "100, 0, 100"
    })
    void search_variousPageSizes_returnsCorrectResults(int pageSize, int pageNumber, int expectedSize) {

        String searchTerm = "laptop";
        Pageable customPageable = PageRequest.of(pageNumber, pageSize);
        Page<ProductDocument> page = new PageImpl<>(
                Collections.singletonList(productDocument1),
                customPageable,
                1
        );

        when(productRepository.wildcardSearch(searchTerm, customPageable)).thenReturn(page);

        SearchResponse result = searchService.search(searchTerm, customPageable);

        assertNotNull(result);
        assertEquals(expectedSize, result.getMetadata().getSize());
        assertEquals(pageNumber, result.getMetadata().getPage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"laptop", "LAPTOP", "Laptop", "LaPtOp"})
    void search_caseInsensitiveSearch_findsResults(String searchTerm) {

        when(productRepository.wildcardSearch(searchTerm, pageable)).thenReturn(productPage);

        SearchResponse result = searchService.search(searchTerm, pageable);

        assertNotNull(result);
        assertFalse(result.getProducts().isEmpty());
        verify(productRepository).wildcardSearch(searchTerm, pageable);
    }

    @Test
    void search_whitespaceOnlyTerm_throwsSearchException() {

        String whitespaceTerm = "   ";

        SearchException exception = assertThrows(
                SearchException.class,
                () -> searchService.search(whitespaceTerm, pageable)
        );

        assertEquals("Search term cannot be null or empty", exception.getMessage());
    }

    @Test
    void searchWithPriorityAndPriceSort_validAscOrder_acceptsLowercase() {

        String searchTerm = "laptop";
        String sortOrder = "asc";
        when(productRepository.wildcardSearchWithPriorityAndPriceSort(searchTerm, pageable, sortOrder))
                .thenReturn(productPage);

        SearchResponse result = searchService.searchWithPriorityAndPriceSort(searchTerm, pageable, sortOrder);

        assertNotNull(result);
        verify(productRepository).wildcardSearchWithPriorityAndPriceSort(searchTerm, pageable, sortOrder);
    }

    @Test
    void searchWithPriorityAndPriceSort_validDescOrder_acceptsLowercase() {

        String searchTerm = "laptop";
        String sortOrder = "desc";
        when(productRepository.wildcardSearchWithPriorityAndPriceSort(searchTerm, pageable, sortOrder))
                .thenReturn(productPage);

        SearchResponse result = searchService.searchWithPriorityAndPriceSort(searchTerm, pageable, sortOrder);

        assertNotNull(result);
        verify(productRepository).wildcardSearchWithPriorityAndPriceSort(searchTerm, pageable, sortOrder);
    }

    @Test
    void search_paginationMetadata_calculatedCorrectly() {

        String searchTerm = "laptop";
        Pageable customPageable = PageRequest.of(2, 5);
        List<ProductDocument> products = Arrays.asList(productDocument1, productDocument2);
        Page<ProductDocument> page = new PageImpl<>(products, customPageable, 12);

        when(productRepository.wildcardSearch(searchTerm, customPageable)).thenReturn(page);

        SearchResponse result = searchService.search(searchTerm, customPageable);

        PaginationMetadata metadata = result.getMetadata();
        assertNotNull(metadata);
        assertEquals(2, metadata.getPage());
        assertEquals(5, metadata.getSize());
        assertEquals(2, metadata.getTotalItems());
        assertEquals(3, metadata.getTotalPage());
    }
}
