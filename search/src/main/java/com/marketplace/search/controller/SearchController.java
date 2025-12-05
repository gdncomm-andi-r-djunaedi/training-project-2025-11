package com.marketplace.search.controller;

import com.marketplace.search.dto.SearchResponse;
import com.marketplace.search.service.SearchService;
import com.marketplace.search.util.ApiResponse;
import com.marketplace.search.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<ApiResponse<SearchResponse>> search(
            @RequestParam("q") String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        SearchResponse response = searchService.search(query, page, size);
        return ResponseUtil.success(response, "Search completed successfully");
    }
}
