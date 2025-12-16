package com.example.search.repository;

import com.example.search.entity.ProductDocument;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductRepository {

    private final SolrClient solrClient;

    public ProductRepository(SolrClient solrClient) {
        this.solrClient = solrClient;
    }

    public void save(ProductDocument product) {
        try {
            solrClient.addBean(product);
            solrClient.commit();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save product to Solr", e);
        }
    }

    public void deleteByProductId(long productId) {
        try {
            solrClient.deleteByQuery("productId:" + productId);
            solrClient.commit();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete product from Solr", e);
        }
    }

    public Page<ProductDocument> wildcardSearch(String searchTerm, Pageable pageable) {
        try {
            SolrQuery query = new SolrQuery();
            String q = String.format("title:*%s* OR category:*%s* OR description:*%s*", searchTerm, searchTerm, searchTerm);
            query.setQuery(q);
            query.setStart((int) pageable.getOffset());
            query.setRows(pageable.getPageSize());

            QueryResponse response = solrClient.query(query);
            List<ProductDocument> products = response.getBeans(ProductDocument.class);
            long total = response.getResults().getNumFound();

            return new PageImpl<>(products, pageable, total);
        } catch (Exception e) {
            throw new RuntimeException("Failed to search products in Solr", e);
        }
    }

    public Page<ProductDocument> wildcardSearchWithPriority(String searchTerm, Pageable pageable) {
        try {
            SolrQuery query = new SolrQuery();
            String q = String.format("title:*%s*^3 OR category:*%s*^2 OR description:*%s*", 
                    searchTerm, searchTerm, searchTerm);
            query.setQuery(q);
            query.setStart((int) pageable.getOffset());
            query.setRows(pageable.getPageSize());

            QueryResponse response = solrClient.query(query);
            List<ProductDocument> products = response.getBeans(ProductDocument.class);
            long total = response.getResults().getNumFound();

            return new PageImpl<>(products, pageable, total);
        } catch (Exception e) {
            throw new RuntimeException("Failed to search products with priority in Solr", e);
        }
    }

    public Page<ProductDocument> wildcardSearchWithPriorityAndPriceSort(String searchTerm, Pageable pageable, String sortOrder) {
        try {
            SolrQuery query = new SolrQuery();
            String q = String.format("title:*%s*^3 OR category:*%s*^2",
                    searchTerm, searchTerm);
            query.setQuery(q);

            if ("asc".equalsIgnoreCase(sortOrder)) {
                query.setSort("price", SolrQuery.ORDER.asc);
            } else if ("desc".equalsIgnoreCase(sortOrder)) {
                query.setSort("price", SolrQuery.ORDER.desc);
            }
            
            query.setStart((int) pageable.getOffset());
            query.setRows(pageable.getPageSize());

            QueryResponse response = solrClient.query(query);
            List<ProductDocument> products = response.getBeans(ProductDocument.class);
            long total = response.getResults().getNumFound();

            return new PageImpl<>(products, pageable, total);
        } catch (Exception e) {
            throw new RuntimeException("Failed to search products with priority and price sort in Solr", e);
        }
    }
}
