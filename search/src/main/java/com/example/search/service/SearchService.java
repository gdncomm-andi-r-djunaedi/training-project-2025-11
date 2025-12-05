package com.example.search.service;

import com.example.search.dto.SearchResponse;
import org.springframework.data.domain.Pageable;

public interface SearchService {

    SearchResponse search(String term, Pageable pageable);

    SearchResponse searchWithPriority(String term, Pageable pageable);

    SearchResponse searchWithPriorityAndPriceSort(String term, Pageable pageable, String sortOrder);
}
