package com.gdn.training.product.infrastructure.config;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SolrConfig {
    // Attributes
    private final String solrUrl;

    // Constructor
    public SolrConfig(
            @Value("${solr.host:http://localhost:8983/solr}") String solrUrl) {
        this.solrUrl = solrUrl;
    }

    // Methods
    @Bean
    public SolrClient solrClient() {
        return new HttpSolrClient.Builder(solrUrl).build();
    }
}
