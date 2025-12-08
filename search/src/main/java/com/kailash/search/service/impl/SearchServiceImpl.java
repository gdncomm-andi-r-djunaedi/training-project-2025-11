package com.kailash.search.service.impl;

import com.kailash.search.dto.ProductPayload;
import com.kailash.search.service.SearchService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {

    private final ElasticsearchOperations esOps;

    @Value("${indexer.es.index-name:products}")
    private String indexName;

    public SearchServiceImpl(ElasticsearchOperations esOps) {
        this.esOps = esOps;
    }

    @Override
    public ProductPayload getById(String id) {
        return esOps.get(id, ProductPayload.class, IndexCoordinates.of(indexName));
    }

    @Override
    public List<ProductPayload> searchByName(String text) {

        Pageable pageable = PageRequest.of(0, 20);

        String wildcard = "*" + text.toLowerCase() + "*";

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> b
                                .should(s -> s
                                        .multiMatch(m -> m
                                                .query(text)
                                                .fields("name^3", "shortDescription")
                                                .fuzziness("AUTO")
                                        )
                                )
                                .should(s -> s
                                        .wildcard(w -> w
                                                .field("name.keyword")
                                                .value(wildcard)
                                        )
                                )
                                .should(s -> s
                                        .wildcard(w -> w
                                                .field("shortDescription.keyword")
                                                .value(wildcard)
                                        )
                                )
                                .minimumShouldMatch("1")
                        )
                )
                .withPageable(pageable)
                .build();

        SearchHits<ProductPayload> hits = esOps.search(
                query,
                ProductPayload.class,
                IndexCoordinates.of(indexName)
        );

        return hits.getSearchHits()
                .stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

}
