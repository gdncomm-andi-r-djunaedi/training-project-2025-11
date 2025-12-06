package com.gdn.training.product.infrastructure.search;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.gdn.training.product.domain.model.Product;
import com.gdn.training.product.domain.specification.ProductSpecification;

@Component
public class SolrProductSearchRepository {
    // Attributes
    private final SolrClient solrClient;
    private final String collection;

    // Constructor
    public SolrProductSearchRepository(
            SolrClient solrClient,
            @Value("${solr.collection:products}") String collection) {
        this.solrClient = solrClient;
        this.collection = collection;
    }

    // Methods
    public List<Product> search(ProductSpecification spec, int page, int size, String sort) {
        try {
            SolrQuery query = buildSolrQuery(spec, page, size, sort);
            QueryResponse response = solrClient.query(collection, query);
            List<Product> products = new ArrayList<>();
            for (SolrDocument doc : response.getResults()) {
                String pid = (String) doc.getFieldValue("productId");
                String sellerId = (String) doc.getFieldValue("sellerId");
                String name = (String) doc.getFieldValue("name");
                String description = (String) doc.getFieldValue("description");
                String category = (String) doc.getFieldValue("category");
                Double price = doc.getFieldValue("price") != null ? ((Number) doc.getFieldValue("price")).doubleValue()
                        : 0.0;
                Integer stock = doc.getFieldValue("stock") != null ? ((Number) doc.getFieldValue("stock")).intValue()
                        : 0;
                String status = (String) doc.getFieldValue("status");
                Object created = doc.getFieldValue("createdAt");
                long createdMillis = created instanceof java.util.Date ? ((java.util.Date) created).getTime()
                        : Instant.now().toEpochMilli();

                Product product = new Product(
                        UUID.fromString(pid),
                        UUID.fromString(sellerId),
                        name,
                        description,
                        category,
                        price,
                        stock,
                        Product.Status.valueOf(status),
                        Instant.ofEpochMilli(createdMillis));
                products.add(product);
            }
            return products;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public long count(ProductSpecification spec) {
        try {
            SolrQuery query = buildSolrQuery(spec, 0, 0, null);
            return solrClient.query(collection, query).getResults().getNumFound();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private SolrQuery buildSolrQuery(ProductSpecification spec, int page, int size, String sortBy) {
        StringBuilder sb = new StringBuilder();
        // keyword => search across name and description with wildcard
        if (spec.keyword() != null && !spec.keyword().isBlank()) {
            String escaped = spec.keyword().replaceAll("[+\\-!(){}\\[\\]^\"~*?:\\\\/]", " ");
            sb.append(String.format("name:*%s* OR description:*%s*", escaped, escaped));
        } else {
            sb.append("*:*");
        }

        if (spec.category() != null && !spec.category().isBlank()) {
            sb.append(" AND category:").append(spec.category());
        }
        if (spec.minPrice() >= 0) {
            sb.append(" AND price:[").append(spec.minPrice()).append(" TO *]");
        }
        if (spec.maxPrice() >= 0) {
            sb.append(" AND price:[* TO ").append(spec.maxPrice()).append("]");
        }

        SolrQuery q = new SolrQuery();
        q.setQuery(sb.toString());
        if (size > 0)
            q.setRows(size);
        q.setStart(page * Math.max(1, size));
        if (sortBy != null && !sortBy.isBlank()) {
            // expect "price:asc" or "price:desc"
            String[] parts = sortBy.split(":");
            if (parts.length == 2)
                q.setSort(parts[0], "asc".equalsIgnoreCase(parts[1]) ? SolrQuery.ORDER.asc : SolrQuery.ORDER.desc);
        }
        return q;
    }
}
