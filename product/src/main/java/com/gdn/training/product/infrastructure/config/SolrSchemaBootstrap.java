package com.gdn.training.product.infrastructure.config;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * Very small bootstrap helper that adds fields to Solr schema via Schema API if
 * needed.
 * It's intentionally simple: it will attempt to add required fields on startup.
 */
@Component
public class SolrSchemaBootstrap {
    // solr base url
    private final String solrBase;

    // constructor
    public SolrSchemaBootstrap(
            @Value("${spring.data.solr.host:http://localhost:8983/solr/products}") String solrBase) {
        this.solrBase = solrBase;
    }

    //
    @PostConstruct
    public void ensureSchema() {
        try {
            // Add minimal fields: productId (string), sellerId (string), name, description,
            // category, price, stock, status, createdAt
            RestTemplate rt = new RestTemplate();
            String url = solrBase + "/schema";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // We'll add fields one by one (idempotent if already exists)
            String payload = """
                    {
                      "add-field":[
                        {"name":"productId","type":"string","stored":true,"indexed":true},
                        {"name":"sellerId","type":"string","stored":true,"indexed":true},
                        {"name":"name","type":"text_general","stored":true,"indexed":true},
                        {"name":"description","type":"text_general","stored":true,"indexed":true},
                        {"name":"category","type":"string","stored":true,"indexed":true},
                        {"name":"price","type":"pdouble","stored":true,"indexed":true},
                        {"name":"stock","type":"pint","stored":true,"indexed":true},
                        {"name":"status","type":"string","stored":true,"indexed":true},
                        {"name":"createdAt","type":"pdate","stored":true,"indexed":true}
                      ]
                    }
                    """;

            HttpEntity<String> entity = new HttpEntity<>(payload, headers);
            rt.postForEntity(url, entity, String.class);
        } catch (Exception ignored) {
            // ignore: Solr might not be up yet — schema will be ready when solr admin is
            // available
        }
    }

}
