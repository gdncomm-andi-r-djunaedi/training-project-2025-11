package com.kailash.search.service;

import com.kailash.search.dto.ProductPayload;
import java.util.List;

public interface SearchService {
    ProductPayload getById(String id);
    List<ProductPayload> searchByName(String text);
}
