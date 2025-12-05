package com.marketplace.search.service;

import com.marketplace.search.dto.SearchResponse;

public interface SearchService {
    SearchResponse search(String query, int page, int size);
}
