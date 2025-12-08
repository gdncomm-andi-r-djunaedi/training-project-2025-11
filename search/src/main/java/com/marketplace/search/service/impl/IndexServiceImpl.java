package com.marketplace.search.service.impl;

import com.marketplace.search.document.ProductDocument;
import com.marketplace.search.repository.ProductDocumentRepository;
import com.marketplace.search.service.IndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndexServiceImpl implements IndexService {

    private final ProductDocumentRepository productDocumentRepository;

    @Override
    public void saveProduct(ProductDocument document) {
        productDocumentRepository.save(document);
        log.info("Product indexed in Elasticsearch: {}", document.getProductId());
    }

    @Override
    public void deleteProduct(String productId) {
        productDocumentRepository.deleteById(productId);
        log.info("Product deleted from Elasticsearch: {}", productId);
    }
}
