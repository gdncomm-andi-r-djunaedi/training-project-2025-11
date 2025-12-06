package com.gdn.training.product.integration;

import com.gdn.training.product.domain.model.Product;
import com.gdn.training.product.domain.specification.ProductSpecification;
import com.gdn.training.product.infrastructure.search.SolrProductSearchRepository;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.SolrContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for Solr search functionality using Testcontainers.
 * Tests actual Solr queries with a real Solr instance.
 */
@SpringBootTest
@Testcontainers
public class SolrProductSearchRepositoryIntegrationTest {

    @Container
    static SolrContainer solrContainer = new SolrContainer(DockerImageName.parse("solr:9.6"))
            .withCollection("products");

    @DynamicPropertySource
    static void configureSolr(DynamicPropertyRegistry registry) {
        String solrUrl = String.format("http://%s:%d/solr/products",
                solrContainer.getHost(),
                solrContainer.getSolrPort());
        registry.add("spring.data.solr.host", () -> solrUrl);
    }

    @Autowired
    private SolrProductSearchRepository solrProductSearchRepository;

    @Autowired
    private SolrClient solrClient;

    @BeforeEach
    void setUp() throws Exception {
        // Clear Solr collection before each test
        solrClient.deleteByQuery("products", "*:*");
        solrClient.commit("products");
    }

    @Test
    void shouldFindProductsByKeyword() throws Exception {
        // Arrange
        indexProduct("laptop-1", "Gaming Laptop", "electronics", 15000000);
        indexProduct("laptop-2", "Business Laptop", "electronics", 10000000);
        indexProduct("mouse-1", "Gaming Mouse", "electronics", 500000);

        ProductSpecification spec = new ProductSpecification("laptop", null, 0, Double.MAX_VALUE);

        // Act
        List<Product> results = solrProductSearchRepository.search(spec, 0, 10, null);

        // Assert
        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(p -> p.getName().equals("Gaming Laptop")));
        assertTrue(results.stream().anyMatch(p -> p.getName().equals("Business Laptop")));
    }

    @Test
    void shouldFilterByCategory() throws Exception {
        // Arrange
        indexProduct("prod-1", "Book A", "books", 50000);
        indexProduct("prod-2", "Laptop", "electronics", 10000000);
        indexProduct("prod-3", "Book B", "books", 75000);

        ProductSpecification spec = new ProductSpecification(null, "books", 0, Double.MAX_VALUE);

        // Act
        List<Product> results = solrProductSearchRepository.search(spec, 0, 10, null);

        // Assert
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(p -> p.getCategory().equals("books")));
    }

    @Test
    void shouldFilterByPriceRange() throws Exception {
        // Arrange
        indexProduct("prod-1", "Cheap Item", "misc", 10000);
        indexProduct("prod-2", "Mid Item", "misc", 50000);
        indexProduct("prod-3", "Expensive Item", "misc", 100000);

        ProductSpecification spec = new ProductSpecification(null, null, 20000, 60000);

        // Act
        List<Product> results = solrProductSearchRepository.search(spec, 0, 10, null);

        // Assert
        assertEquals(1, results.size());
        assertEquals("Mid Item", results.get(0).getName());
        assertEquals(50000, results.get(0).getPrice());
    }

    @Test
    void shouldHandlePagination() throws Exception {
        // Arrange
        for (int i = 0; i < 25; i++) {
            indexProduct("prod-" + i, "Product " + i, "test", 1000 * i);
        }

        ProductSpecification spec = new ProductSpecification(null, "test", 0, Double.MAX_VALUE);

        // Act
        List<Product> page1 = solrProductSearchRepository.search(spec, 0, 10, null);
        List<Product> page2 = solrProductSearchRepository.search(spec, 10, 10, null);
        List<Product> page3 = solrProductSearchRepository.search(spec, 20, 10, null);

        // Assert
        assertEquals(10, page1.size());
        assertEquals(10, page2.size());
        assertEquals(5, page3.size());
    }

    @Test
    void shouldReturnEmptyList_WhenNoMatches() throws Exception {
        // Arrange
        indexProduct("prod-1", "Product A", "category-a", 1000);

        ProductSpecification spec = new ProductSpecification("nonexistent", null, 0, Double.MAX_VALUE);

        // Act
        List<Product> results = solrProductSearchRepository.search(spec, 0, 10, null);

        // Assert
        assertTrue(results.isEmpty());
    }

    @Test
    void shouldCombineMultipleFilters() throws Exception {
        // Arrange
        indexProduct("prod-1", "Gaming Laptop", "electronics", 15000000);
        indexProduct("prod-2", "Gaming Mouse", "electronics", 500000);
        indexProduct("prod-3", "Office Laptop", "electronics", 8000000);
        indexProduct("prod-4", "Gaming Chair", "furniture", 2000000);

        ProductSpecification spec = new ProductSpecification("gaming", "electronics", 1000000, 20000000);

        // Act
        List<Product> results = solrProductSearchRepository.search(spec, 0, 10, null);

        // Assert
        assertEquals(1, results.size());
        assertEquals("Gaming Laptop", results.get(0).getName());
    }

    private void indexProduct(String id, String name, String category, double price) throws Exception {
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id", id);
        doc.addField("productId", UUID.randomUUID().toString());
        doc.addField("sellerId", UUID.randomUUID().toString());
        doc.addField("name", name);
        doc.addField("description", "Description for " + name);
        doc.addField("category", category);
        doc.addField("price", price);
        doc.addField("stock", 10);
        doc.addField("status", "ACTIVE");
        doc.addField("createdAt", Instant.now());

        solrClient.add("products", doc);
        solrClient.commit("products");
    }
}
