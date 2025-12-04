package com.blibli.search.services;

import com.blibli.search.entity.elasticsearch.ElasticsearchProductDocument;
import com.blibli.search.exception.ElasticsearchException;
import com.blibli.search.exception.SearchException;
import com.blibli.search.repository.elasticsearch.ElasticsearchProductRepository;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Elasticsearch Search Service Tests")

class ElasticsearchSearchServiceTest {

    @Mock
    private ElasticsearchProductRepository elasticsearchProductRepository;

    @Mock
    private ElasticsearchClient elasticsearchClient;

    @InjectMocks
    private ElasticsearchSearchService elasticsearchSearchService;

    private static final String PRODUCT_ID = "product-123";
    private static final String PRODUCT_NAME = "Test Product";
    private static final String PRODUCT_DESCRIPTION = "Test Description";
    private static final Double PRODUCT_PRICE = 99.99;
    private static final String PRODUCT_CATEGORY = "ELECTRONIC";

    private Map<String, Object> productData;
    private ElasticsearchProductDocument productDocument;

    @BeforeEach
    void setUp() {
        productData = new HashMap<>();
        productData.put("id", PRODUCT_ID);
        productData.put("name", PRODUCT_NAME);
        productData.put("description", PRODUCT_DESCRIPTION);
        productData.put("price", PRODUCT_PRICE);
        productData.put("category", PRODUCT_CATEGORY);

        productDocument = ElasticsearchProductDocument.builder()
                .id(PRODUCT_ID)
                .name(PRODUCT_NAME)
                .description(PRODUCT_DESCRIPTION)
                .price(PRODUCT_PRICE)
                .category(PRODUCT_CATEGORY)
                .build();
    }


    @Test
    @DisplayName("Should index product successfully")
    void testIndexProductSuccess() {
        when(elasticsearchProductRepository.save(any(ElasticsearchProductDocument.class)))
                .thenReturn(productDocument);
        when(elasticsearchProductRepository.existsById(PRODUCT_ID)).thenReturn(true);

        elasticsearchSearchService.indexProduct(productData);

        verify(elasticsearchProductRepository).save(any(ElasticsearchProductDocument.class));
        verify(elasticsearchProductRepository).existsById(PRODUCT_ID);
    }

    @Test
    @DisplayName("Should handle price as BigDecimal")
    void indexProductWithBigDecimalPrice() {
        productData.put("price", new BigDecimal("199.99"));
        when(elasticsearchProductRepository.save(any(ElasticsearchProductDocument.class)))
                .thenReturn(productDocument);
        when(elasticsearchProductRepository.existsById(PRODUCT_ID)).thenReturn(true);

        elasticsearchSearchService.indexProduct(productData);

        verify(elasticsearchProductRepository).save(any(ElasticsearchProductDocument.class));
    }

    @Test
    @DisplayName("Should handle null price")
    void indexProductWithNullPrice() {
        productData.put("price", null);
        when(elasticsearchProductRepository.save(any(ElasticsearchProductDocument.class)))
                .thenReturn(productDocument);
        when(elasticsearchProductRepository.existsById(PRODUCT_ID)).thenReturn(true);

        elasticsearchSearchService.indexProduct(productData);

        verify(elasticsearchProductRepository).save(any(ElasticsearchProductDocument.class));
    }

    @Test
    @DisplayName("Should handle category as Enum")
    void indexProductWithEnumCategory() {
        productData.put("category", CategoryEnum.ELECTRONIC);
        when(elasticsearchProductRepository.save(any(ElasticsearchProductDocument.class)))
                .thenReturn(productDocument);
        when(elasticsearchProductRepository.existsById(PRODUCT_ID)).thenReturn(true);

        elasticsearchSearchService.indexProduct(productData);

        verify(elasticsearchProductRepository).save(any(ElasticsearchProductDocument.class));
    }

    @Test
    @DisplayName("Should handle category as Map")
    void indexProductWithMapCategory() {

        Map<String, String> categoryMap = new HashMap<>();
        categoryMap.put("name", PRODUCT_CATEGORY);
        productData.put("category", categoryMap);
        when(elasticsearchProductRepository.save(any(ElasticsearchProductDocument.class)))
                .thenReturn(productDocument);
        when(elasticsearchProductRepository.existsById(PRODUCT_ID)).thenReturn(true);

        elasticsearchSearchService.indexProduct(productData);


        verify(elasticsearchProductRepository).save(any(ElasticsearchProductDocument.class));
    }

    @Test
    @DisplayName("Should throw ElasticsearchException when indexing fails")
    void indexProductThrowsExceptionOnFailure() {
        when(elasticsearchProductRepository.save(any(ElasticsearchProductDocument.class)))
                .thenThrow(new RuntimeException("Elasticsearch connection failed"));

        assertThatThrownBy(() -> elasticsearchSearchService.indexProduct(productData))
                .isInstanceOf(ElasticsearchException.class)
                .hasMessageContaining("Failed to index product in Elasticsearch");

        verify(elasticsearchProductRepository).save(any(ElasticsearchProductDocument.class));
    }


    @Test
    @DisplayName("Should delete product successfully")
    void testDeleteProduct() {
        doNothing().when(elasticsearchProductRepository).deleteById(PRODUCT_ID);

        elasticsearchSearchService.deleteProduct(PRODUCT_ID);

        verify(elasticsearchProductRepository).deleteById(PRODUCT_ID);
    }

    @Test
    @DisplayName("Should throw ElasticsearchException when deletion fails")
    void deleteProductThrowsExceptionOnFailure() {
        doThrow(new RuntimeException("Elasticsearch connection failed"))
                .when(elasticsearchProductRepository).deleteById(PRODUCT_ID);

        assertThatThrownBy(() -> elasticsearchSearchService.deleteProduct(PRODUCT_ID))
                .isInstanceOf(ElasticsearchException.class)
                .hasMessageContaining("Failed to delete product from Elasticsearch");

        verify(elasticsearchProductRepository).deleteById(PRODUCT_ID);
    }


    @Test
    @DisplayName("Should search by name successfully")
    void searchByNameReturnsResults() {
        PageRequest pageable = PageRequest.of(0, 10);
        List<ElasticsearchProductDocument> documents = Arrays.asList(productDocument);
        Page<ElasticsearchProductDocument> page = new PageImpl<>(documents, pageable, 1);

        when(elasticsearchProductRepository.findByNameContaining(PRODUCT_NAME, pageable))
                .thenReturn(page);

        Page<ElasticsearchProductDocument> result = elasticsearchSearchService.searchByName(PRODUCT_NAME, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo(PRODUCT_NAME);
        verify(elasticsearchProductRepository).findByNameContaining(PRODUCT_NAME, pageable);
    }

    @Test
    @DisplayName("Should return empty page when no results found")
    void searchByNameReturnsEmptyWhenNoResults() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<ElasticsearchProductDocument> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(elasticsearchProductRepository.findByNameContaining("NonExistent", pageable))
                .thenReturn(emptyPage);

        Page<ElasticsearchProductDocument> result = elasticsearchSearchService.searchByName("NonExistent", pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        verify(elasticsearchProductRepository).findByNameContaining("NonExistent", pageable);
    }


    @Test
    @DisplayName("Should perform wildcard search successfully")
    void testWildcardSearch() throws Exception {
        String query = "test";
        int page = 0;
        int size = 10;

        Map<String, Object> source = new HashMap<>();
        source.put("id", PRODUCT_ID);
        source.put("name", PRODUCT_NAME);
        source.put("description", PRODUCT_DESCRIPTION);
        source.put("price", PRODUCT_PRICE);
        source.put("category", PRODUCT_CATEGORY);

        Hit<Map> hit = mock(Hit.class);
        when(hit.source()).thenReturn(source);

        HitsMetadata<Map> hitsMetadata = mock(HitsMetadata.class);
        when(hitsMetadata.hits()).thenReturn(Arrays.asList(hit));
        TotalHits totalHits = mock(TotalHits.class);
        when(totalHits.value()).thenReturn(1L);
        when(hitsMetadata.total()).thenReturn(totalHits);


        SearchResponse<Map> searchResponse = mock(SearchResponse.class);
        when(searchResponse.hits()).thenReturn(hitsMetadata);

        when(elasticsearchClient.search(any(SearchRequest.class), eq(Map.class)))
                .thenReturn(searchResponse);

        List<Map<String, Object>> results = elasticsearchSearchService.wildcardSearch(query, page, size);

        assertThat(results).isNotNull();
        assertThat(results).hasSize(1);
        assertThat(results.get(0).get("id")).isEqualTo(PRODUCT_ID);
        assertThat(results.get(0).get("name")).isEqualTo(PRODUCT_NAME);
        verify(elasticsearchClient).search(any(SearchRequest.class), eq(Map.class));
    }

    @Test
    @DisplayName("Should handle empty results in wildcard search")
    void wildcardSearchReturnsEmptyWhenNoMatches() throws Exception {

        String query = "nonexistent";
        int page = 0;
        int size = 10;

        HitsMetadata<Map> hitsMetadata = mock(HitsMetadata.class);
        when(hitsMetadata.hits()).thenReturn(Collections.emptyList());
        TotalHits totalHits = mock(TotalHits.class);
        when(totalHits.value()).thenReturn(0L);
        when(hitsMetadata.total()).thenReturn(totalHits);

        SearchResponse<Map> searchResponse = mock(SearchResponse.class);
        when(searchResponse.hits()).thenReturn(hitsMetadata);

        when(elasticsearchClient.search(any(SearchRequest.class), eq(Map.class)))
                .thenReturn(searchResponse);

        List<Map<String, Object>> results = elasticsearchSearchService.wildcardSearch(query, page, size);

        assertThat(results).isNotNull();
        assertThat(results).isEmpty();
        verify(elasticsearchClient).search(any(SearchRequest.class), eq(Map.class));
    }

    @Test
    @DisplayName("Should trim query in wildcard search")
    void wildcardSearchTrimsWhitespace() throws Exception {

        String query = "  test  ";
        int page = 0;
        int size = 10;

        HitsMetadata<Map> hitsMetadata = mock(HitsMetadata.class);
        when(hitsMetadata.hits()).thenReturn(Collections.emptyList());
        TotalHits totalHits = mock(TotalHits.class);
        when(totalHits.value()).thenReturn(0L);
        when(hitsMetadata.total()).thenReturn(totalHits);

        SearchResponse<Map> searchResponse = mock(SearchResponse.class);
        when(searchResponse.hits()).thenReturn(hitsMetadata);

        when(elasticsearchClient.search(any(SearchRequest.class), eq(Map.class)))
                .thenReturn(searchResponse);

        elasticsearchSearchService.wildcardSearch(query, page, size);

        verify(elasticsearchClient).search(any(SearchRequest.class), eq(Map.class));
    }

    @Test
    @DisplayName("Should throw SearchException when wildcard search fails")
    void wildcardSearchThrowsExceptionOnError() throws Exception {

        String query = "test";
        int page = 0;
        int size = 10;

        when(elasticsearchClient.search(any(SearchRequest.class), eq(Map.class)))
                .thenThrow(new RuntimeException("Elasticsearch connection failed"));


        assertThatThrownBy(() -> elasticsearchSearchService.wildcardSearch(query, page, size))
                .isInstanceOf(SearchException.class)
                .hasMessageContaining("Wildcard search failed");

        verify(elasticsearchClient).search(any(SearchRequest.class), eq(Map.class));
    }

    @Test
    @DisplayName("Should handle null total hits in wildcard search")
    void wildcardSearchHandlesNullTotalHits() throws Exception {

        String query = "test";
        int page = 0;
        int size = 10;

        Hit<Map> hit = mock(Hit.class);
        Map<String, Object> source = new HashMap<>();
        source.put("id", PRODUCT_ID);
        source.put("name", PRODUCT_NAME);
        when(hit.source()).thenReturn(source);

        HitsMetadata<Map> hitsMetadata = mock(HitsMetadata.class);
        when(hitsMetadata.hits()).thenReturn(Arrays.asList(hit));
        when(hitsMetadata.total()).thenReturn(null);

        SearchResponse<Map> searchResponse = mock(SearchResponse.class);
        when(searchResponse.hits()).thenReturn(hitsMetadata);

        when(elasticsearchClient.search(any(SearchRequest.class), eq(Map.class)))
                .thenReturn(searchResponse);

        List<Map<String, Object>> results = elasticsearchSearchService.wildcardSearch(query, page, size);

        assertThat(results).isNotNull();
        assertThat(results).hasSize(1);
        verify(elasticsearchClient).search(any(SearchRequest.class), eq(Map.class));
    }

    @Test
    @DisplayName("Should perform advanced search successfully")
    void testAdvancedSearch() throws Exception {

        String query = "test";
        int page = 0;
        int size = 10;

        Map<String, Object> source = new HashMap<>();
        source.put("id", PRODUCT_ID);
        source.put("name", PRODUCT_NAME);
        source.put("description", PRODUCT_DESCRIPTION);
        source.put("price", PRODUCT_PRICE);
        source.put("category", PRODUCT_CATEGORY);

        Hit<Map> hit = mock(Hit.class);
        when(hit.source()).thenReturn(source);

        HitsMetadata<Map> hitsMetadata = mock(HitsMetadata.class);
        when(hitsMetadata.hits()).thenReturn(Arrays.asList(hit));
        TotalHits totalHits = mock(TotalHits.class);
        when(totalHits.value()).thenReturn(1L);
        when(hitsMetadata.total()).thenReturn(totalHits);
        SearchResponse<Map> searchResponse = mock(SearchResponse.class);
        when(searchResponse.hits()).thenReturn(hitsMetadata);

        when(elasticsearchClient.search(any(SearchRequest.class), eq(Map.class)))
                .thenReturn(searchResponse);
        List<Map<String, Object>> results = elasticsearchSearchService.advancedSearch(query, page, size);
        assertThat(results).isNotNull();
        assertThat(results).hasSize(1);
        assertThat(results.get(0).get("id")).isEqualTo(PRODUCT_ID);
        assertThat(results.get(0).get("name")).isEqualTo(PRODUCT_NAME);
        verify(elasticsearchClient).search(any(SearchRequest.class), eq(Map.class));
    }

    @Test
    @DisplayName("Should handle empty results in advanced search")
    void advancedSearchReturnsEmptyWhenNoResults() throws Exception {
        String query = "nonexistent";
        int page = 0;
        int size = 10;

        HitsMetadata<Map> hitsMetadata = mock(HitsMetadata.class);
        when(hitsMetadata.hits()).thenReturn(Collections.emptyList());
        TotalHits totalHits = mock(TotalHits.class);
        when(totalHits.value()).thenReturn(0L);
        when(hitsMetadata.total()).thenReturn(totalHits);


        SearchResponse<Map> searchResponse = mock(SearchResponse.class);
        when(searchResponse.hits()).thenReturn(hitsMetadata);

        when(elasticsearchClient.search(any(SearchRequest.class), eq(Map.class)))
                .thenReturn(searchResponse);

        List<Map<String, Object>> results = elasticsearchSearchService.advancedSearch(query, page, size);

        assertThat(results).isNotNull();
        assertThat(results).isEmpty();
        verify(elasticsearchClient).search(any(SearchRequest.class), eq(Map.class));
    }

    @Test
    @DisplayName("Should throw SearchException when advanced search fails")
    void advancedSearchThrowsExceptionOnError() throws Exception {

        String query = "test";
        int page = 0;
        int size = 10;

        when(elasticsearchClient.search(any(SearchRequest.class), eq(Map.class)))
                .thenThrow(new RuntimeException("Elasticsearch connection failed"));

        assertThatThrownBy(() -> elasticsearchSearchService.advancedSearch(query, page, size))
                .isInstanceOf(SearchException.class)
                .hasMessageContaining("Advanced search failed");

        verify(elasticsearchClient).search(any(SearchRequest.class), eq(Map.class));
    }

    @Test
    @DisplayName("Should handle null total hits in advanced search")
    void advancedSearchHandlesNullTotalHits() throws Exception {
        String query = "test";
        int page = 0;
        int size = 10;

        Hit<Map> hit = mock(Hit.class);
        Map<String, Object> source = new HashMap<>();
        source.put("id", PRODUCT_ID);
        source.put("name", PRODUCT_NAME);
        when(hit.source()).thenReturn(source);

        HitsMetadata<Map> hitsMetadata = mock(HitsMetadata.class);
        when(hitsMetadata.hits()).thenReturn(Arrays.asList(hit));
        when(hitsMetadata.total()).thenReturn(null);

        SearchResponse<Map> searchResponse = mock(SearchResponse.class);
        when(searchResponse.hits()).thenReturn(hitsMetadata);

        when(elasticsearchClient.search(any(SearchRequest.class), eq(Map.class)))
                .thenReturn(searchResponse);

        List<Map<String, Object>> results = elasticsearchSearchService.advancedSearch(query, page, size);

        assertThat(results).isNotNull();
        assertThat(results).hasSize(1);
        verify(elasticsearchClient).search(any(SearchRequest.class), eq(Map.class));
    }

    @Test
    @DisplayName("Should handle null source in hit")
    void wildcardSearchHandlesNullSource() throws Exception {
        String query = "test";
        int page = 0;
        int size = 10;

        Hit<Map> hit = mock(Hit.class);
        when(hit.source()).thenReturn(null);

        HitsMetadata<Map> hitsMetadata = mock(HitsMetadata.class);
        when(hitsMetadata.hits()).thenReturn(Arrays.asList(hit));
        TotalHits totalHits = mock(TotalHits.class);
        when(totalHits.value()).thenReturn(1L);
        when(hitsMetadata.total()).thenReturn(totalHits);

        SearchResponse<Map> searchResponse = mock(SearchResponse.class);
        when(searchResponse.hits()).thenReturn(hitsMetadata);

        when(elasticsearchClient.search(any(SearchRequest.class), eq(Map.class)))
                .thenReturn(searchResponse);

        List<Map<String, Object>> results = elasticsearchSearchService.wildcardSearch(query, page, size);

        assertThat(results).isNotNull();
        assertThat(results).isEmpty();
        verify(elasticsearchClient).search(any(SearchRequest.class), eq(Map.class));
    }

    enum CategoryEnum {
        ELECTRONIC, FASHION, FOOD
    }
}

