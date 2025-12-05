package com.blibli.product.config;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SolrClientConfig {

    @Value("${solr.client.url}")
    private String solrClientURL;

    @Bean
    public SolrClient solrClient(){
        return new HttpSolrClient.Builder(solrClientURL).build();
    }

}
