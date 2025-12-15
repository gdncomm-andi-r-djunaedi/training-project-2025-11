package com.blibli.search.controller;

import com.blibli.search.dto.ApiResponse;
import com.blibli.search.exception.BadRequestException;
import com.blibli.search.services.ElasticsearchSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search/elasticsearch")
@RequiredArgsConstructor
public class ElasticsearchSearchController {

    private final ElasticsearchSearchService elasticsearchSearchService;

    @GetMapping("/name")
    public ResponseEntity<ApiResponse<?>> searchByName(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        if (query == null || query.trim().isEmpty()) {
            throw new BadRequestException("Missing required parameter: query. Example: ?query=refrigerator");
        }
        
        return ResponseEntity.ok(ApiResponse.success(elasticsearchSearchService.searchByName(query.trim(), PageRequest.of(page, size))));
    }

    @GetMapping("/wildcard")
    public ResponseEntity<ApiResponse<?>> wildcardSearch(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        if (query == null || query.trim().isEmpty()) {
            throw new BadRequestException("Missing required parameter: query. Example: ?query=refrigerator");
        }
        
        return ResponseEntity.ok(ApiResponse.success(elasticsearchSearchService.wildcardSearch(query.trim(), page, size)));
    }

    @GetMapping("/advanced")
    public ResponseEntity<ApiResponse<?>> advancedSearch(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        if (query == null || query.trim().isEmpty()) {
            throw new BadRequestException("Missing required parameter: query. Example: ?query=refrigerator");
        }
        
        return ResponseEntity.ok(ApiResponse.success(elasticsearchSearchService.advancedSearch(query.trim(), page, size)));
    }
}

