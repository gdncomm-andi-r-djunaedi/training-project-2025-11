package com.blibli.search.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import com.blibli.search.entity.elasticsearch.ElasticsearchProductDocument;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticsearchIndexInitializer {

    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticsearchClient elasticsearchClient;

    @PostConstruct
    public void initializeIndex() {
        try {
            IndexOperations indexOps = elasticsearchOperations.indexOps(ElasticsearchProductDocument.class);
            String indexName = "products";
            
            log.info("========== Initializing Elasticsearch index: {} ==========", indexName);
            boolean indexExists = elasticsearchClient.indices()
                    .exists(ExistsRequest.of(e -> e.index(indexName)))
                    .value();
            
            if (!indexExists) {
                log.info("Index '{}' does not exist. Creating index...", indexName);

                boolean created = indexOps.create();
                if (created) {
                    log.info("Index '{}' created successfully", indexName);
                } else {
                    log.warn("Index '{}' creation returned false", indexName);
                }

                boolean mappingCreated = indexOps.putMapping(indexOps.createMapping(ElasticsearchProductDocument.class));
                if (mappingCreated) {
                    log.info(" Mapping created successfully for index '{}'", indexName);
                }
            } else {
                log.info("Index '{}' already exists", indexName);
            }

            indexOps.refresh();
            log.info("Index '{}' is ready", indexName);
            
        } catch (Exception e) {
            log.error(" Failed to initialize Elasticsearch index: {}", e.getMessage(), e);

        }
    }
}

