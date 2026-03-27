package com.blibli.search.controller;

import com.blibli.search.entity.elasticsearch.ElasticsearchProductDocument;
import com.blibli.search.services.ElasticsearchSearchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ElasticsearchSearchController.class)
@DisplayName("Elasticsearch Search Controller Tests")
class ElasticsearchSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    @SuppressWarnings("deprecation")
    private ElasticsearchSearchService elasticsearchSearchService;

    private static final String PRODUCT_ID = "product-123";
    private static final String PRODUCT_NAME = "Test Product";
    private static final String PRODUCT_DESCRIPTION = "Test Description";
    private static final Double PRODUCT_PRICE = 99.99;
    private static final String PRODUCT_CATEGORY = "ELECTRONIC";



    @Test
    @DisplayName("Should search by name successfully")
    void searchByName_Success() throws Exception {
        // Given
        String query = "test";
        ElasticsearchProductDocument document = ElasticsearchProductDocument.builder()
                .id(PRODUCT_ID)
                .name(PRODUCT_NAME)
                .description(PRODUCT_DESCRIPTION)
                .price(PRODUCT_PRICE)
                .category(PRODUCT_CATEGORY)
                .build();

        Page<ElasticsearchProductDocument> page = new PageImpl<>(
                Arrays.asList(document),
                PageRequest.of(0, 10),
                1
        );

        when(elasticsearchSearchService.searchByName(eq(query), any(PageRequest.class)))
                .thenReturn(page);

        // When/Then
        mockMvc.perform(get("/api/search/elasticsearch/name")
                        .param("query", query)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").value(PRODUCT_ID))
                .andExpect(jsonPath("$.data.content[0].name").value(PRODUCT_NAME));
    }

    @Test
    @DisplayName("Should return 400 when query parameter is missing")
    void searchByName_Failure_MissingQuery() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/search/elasticsearch/name")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Missing required parameter: query. Example: ?query=refrigerator"));
    }

    @Test
    @DisplayName("Should return 400 when query parameter is empty")
    void searchByName_Failure_EmptyQuery() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/search/elasticsearch/name")
                        .param("query", "")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Should return 400 when query parameter is whitespace only")
    void searchByName_Failure_WhitespaceQuery() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/search/elasticsearch/name")
                        .param("query", "   ")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Should use default pagination when not provided")
    void searchByName_Success_DefaultPagination() throws Exception {

        String query = "test";
        Page<ElasticsearchProductDocument> page = new PageImpl<>(
                Collections.emptyList(),
                PageRequest.of(0, 10),
                0
        );

        when(elasticsearchSearchService.searchByName(eq(query), any(PageRequest.class)))
                .thenReturn(page);


        mockMvc.perform(get("/api/search/elasticsearch/name")
                        .param("query", query)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }



    @Test
    @DisplayName("Should perform wildcard search successfully")
    void wildcardSearch_Success() throws Exception {

        String query = "test";
        Map<String, Object> result = new HashMap<>();
        result.put("id", PRODUCT_ID);
        result.put("name", PRODUCT_NAME);
        result.put("description", PRODUCT_DESCRIPTION);
        result.put("price", PRODUCT_PRICE);
        result.put("category", PRODUCT_CATEGORY);

        when(elasticsearchSearchService.wildcardSearch(query, 0, 10))
                .thenReturn(Arrays.asList(result));

        mockMvc.perform(get("/api/search/elasticsearch/wildcard")
                        .param("query", query)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(PRODUCT_ID))
                .andExpect(jsonPath("$.data[0].name").value(PRODUCT_NAME));
    }

    @Test
    @DisplayName("Should return 400 when query parameter is missing in wildcard search")
    void wildcardSearch_Failure_MissingQuery() throws Exception {

        mockMvc.perform(get("/api/search/elasticsearch/wildcard")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Missing required parameter: query. Example: ?query=refrigerator"));
    }

    @Test
    @DisplayName("Should return empty list when no results found in wildcard search")
    void wildcardSearch_Success_NoResults() throws Exception {

        String query = "nonexistent";

        when(elasticsearchSearchService.wildcardSearch(query, 0, 10))
                .thenReturn(Collections.emptyList());


        mockMvc.perform(get("/api/search/elasticsearch/wildcard")
                        .param("query", query)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }



    @Test
    @DisplayName("Should perform advanced search successfully")
    void advancedSearch_Success() throws Exception {

        String query = "test";
        Map<String, Object> result = new HashMap<>();
        result.put("id", PRODUCT_ID);
        result.put("name", PRODUCT_NAME);
        result.put("description", PRODUCT_DESCRIPTION);
        result.put("price", PRODUCT_PRICE);
        result.put("category", PRODUCT_CATEGORY);

        when(elasticsearchSearchService.advancedSearch(query, 0, 10))
                .thenReturn(Arrays.asList(result));


        mockMvc.perform(get("/api/search/elasticsearch/advanced")
                        .param("query", query)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(PRODUCT_ID))
                .andExpect(jsonPath("$.data[0].name").value(PRODUCT_NAME));
    }

    @Test
    @DisplayName("Should return 400 when query parameter is missing in advanced search")
    void advancedSearch_Failure_MissingQuery() throws Exception {

        mockMvc.perform(get("/api/search/elasticsearch/advanced")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Missing required parameter: query. Example: ?query=refrigerator"));
    }

    @Test
    @DisplayName("Should handle pagination correctly in advanced search")
    void advancedSearch_Success_WithPagination() throws Exception {

        String query = "test";
        Map<String, Object> result1 = new HashMap<>();
        result1.put("id", "product-1");
        result1.put("name", "Product 1");

        Map<String, Object> result2 = new HashMap<>();
        result2.put("id", "product-2");
        result2.put("name", "Product 2");

        when(elasticsearchSearchService.advancedSearch(query, 1, 2))
                .thenReturn(Arrays.asList(result1, result2));


        mockMvc.perform(get("/api/search/elasticsearch/advanced")
                        .param("query", query)
                        .param("page", "1")
                        .param("size", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isNotEmpty());
    }
}

