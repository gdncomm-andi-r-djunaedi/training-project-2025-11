package com.blibli.search.controller;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch.core.CountRequest;
import co.elastic.clients.elasticsearch.core.CountResponse;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import com.blibli.search.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/elasticsearch")
@RequiredArgsConstructor
public class ElasticsearchAdminController {

    private final ElasticsearchClient elasticsearchClient;
    private final ElasticsearchOperations elasticsearchOperations;

    @GetMapping("/index/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getIndexStatus() {
        try {
            String indexName = "products";
            boolean exists = elasticsearchClient.indices()
                    .exists(ExistsRequest.of(e -> e.index(indexName)))
                    .value();

            Map<String, Object> status = new HashMap<>();
            status.put("indexName", indexName);
            status.put("exists", exists);

            if (exists) {
                try {
                    CountResponse countResponse = elasticsearchClient.count(
                            CountRequest.of(c -> c.index(indexName)));
                    status.put("documentCount", countResponse.count());
                } catch (Exception e) {
                    log.error("Failed to get document count", e);
                    status.put("documentCount", "error: " + e.getMessage());
                }
            }

            return ResponseEntity.ok(ApiResponse.success(status));
        } catch (Exception e) {
            log.error("Failed to get index status", e);
            return ResponseEntity.ok(ApiResponse.error("Failed to get index status: " + e.getMessage()));
        }
    }

    @PostMapping("/index/create")
    public ResponseEntity<ApiResponse<String>> createIndex() {
        try {
            String indexName = "products";
            log.info("Creating index: {}", indexName);

            boolean exists = elasticsearchClient.indices()
                    .exists(ExistsRequest.of(e -> e.index(indexName)))
                    .value();

            if (exists) {
                return ResponseEntity.ok(ApiResponse.success("Index '" + indexName + "' already exists"));
            }

            // Create index with mapping
            CreateIndexRequest createRequest = CreateIndexRequest.of(c -> c
                    .index(indexName)
                    .mappings(m -> m
                            .properties("id", Property.of(p -> p.keyword(k -> k)))
                            .properties("name", Property.of(p -> p.text(t -> t.analyzer("standard"))))
                            .properties("description", Property.of(p -> p.text(t -> t.analyzer("standard"))))
                            .properties("price", Property.of(p -> p.double_(d -> d)))
                            .properties("category", Property.of(p -> p.keyword(k -> k)))));

            elasticsearchClient.indices().create(createRequest);
            log.info("✅ Index '{}' created successfully", indexName);

            return ResponseEntity.ok(ApiResponse.success("Index '" + indexName + "' created successfully"));
        } catch (Exception e) {
            log.error("Failed to create index", e);
            return ResponseEntity.ok(ApiResponse.error("Failed to create index: " + e.getMessage()));
        }
    }

    @PostMapping("/index/refresh")
    public ResponseEntity<ApiResponse<String>> refreshIndex() {
        try {
            String indexName = "products";
            IndexOperations indexOps = elasticsearchOperations.indexOps(com.blibli.search.entity.elasticsearch.ElasticsearchProductDocument.class);
            indexOps.refresh();
            log.info("✅ Index '{}' refreshed successfully", indexName);
            return ResponseEntity.ok(ApiResponse.success("Index refreshed successfully"));
        } catch (Exception e) {
            log.error("Failed to refresh index", e);
            return ResponseEntity.ok(ApiResponse.error("Failed to refresh index: " + e.getMessage()));
        }
    }
}

