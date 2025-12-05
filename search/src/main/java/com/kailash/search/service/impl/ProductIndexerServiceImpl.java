package com.kailash.search.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kailash.search.dto.ProductEvent;
import com.kailash.search.service.ProductIndexerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.stereotype.Service;

@Service
public class ProductIndexerServiceImpl implements ProductIndexerService {

    private final ElasticsearchOperations esOps;

    @Value("${indexer.es.index-name:products}")
    private String indexName;

    public ProductIndexerServiceImpl(ElasticsearchOperations esOps) {
        this.esOps = esOps;
    }

    @Override
    public void handleEvent(ProductEvent event) {
        if (event == null || event.getEventType() == null) return;
        System.out.println("prinitng event"+event.toString());

        switch (event.getEventType()) {
            case "PRODUCT_UPSERT" -> upsertProduct(event);
            case "PRODUCT_DELETE" -> deleteProduct(event.getProductId());
        }
    }

    @Override
    public void upsertProduct(ProductEvent event) {
        UpdateQuery updateQuery = UpdateQuery.builder(event.getProductId())
                .withDocument(Document.parse(convertToJson(event.getPayload())))
                .withDocAsUpsert(true)
                .build();

        esOps.update(updateQuery, IndexCoordinates.of(indexName));
    }

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private String convertToJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void deleteProduct(String productId) {

        esOps.delete(productId, IndexCoordinates.of(indexName));
    }
}
