package com.example.search.controller;

import com.example.search.dto.SearchResponse;
import com.example.search.service.SearchService;
import com.example.search.utils.APIResponse;
import com.example.search.utils.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired
    SearchService searchService;

    @GetMapping
    public ResponseEntity<APIResponse<SearchResponse>> searchProducts(
            @RequestParam String term,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        Pageable pageable = PageRequest.of(page, size);
        SearchResponse response = searchService.search(term, pageable);
        return ResponseEntity.ok(ResponseUtil.success(response));
    }

    @GetMapping("/priority")
    public ResponseEntity<APIResponse<SearchResponse>> searchWithPriority(
            @RequestParam String term,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        Pageable pageable = PageRequest.of(page, size);
        SearchResponse response = searchService.searchWithPriority(term, pageable);
        return ResponseEntity.ok(ResponseUtil.success(response));
    }

    @GetMapping("/priority-sort")
    public ResponseEntity<APIResponse<SearchResponse>> searchWithPriorityAndPriceSort(
            @RequestParam String term,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "asc") String order) {
        Pageable pageable = PageRequest.of(page, size);
        SearchResponse response = searchService.searchWithPriorityAndPriceSort(term, pageable, order);
        return ResponseEntity.ok(ResponseUtil.success(response));
    }
}
