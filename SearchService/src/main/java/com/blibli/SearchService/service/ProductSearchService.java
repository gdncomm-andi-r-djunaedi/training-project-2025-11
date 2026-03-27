package com.blibli.SearchService.service;

import com.blibli.SearchService.entity.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductSearchService {
    Page<ProductDocument> searchByName(String name, int page, int size);

    ProductDocument searchBySku(String sku);
}
