package com.blibli.gdn.productService.service.impl;

import com.blibli.gdn.productService.model.Product;
import com.blibli.gdn.productService.model.ProductDocument;
import com.blibli.gdn.productService.model.Variant;
import com.blibli.gdn.productService.model.VariantDocument;
import com.blibli.gdn.productService.repository.ProductDocumentRepository;
import com.blibli.gdn.productService.repository.ProductRepository;
import com.blibli.gdn.productService.service.ProductIndexingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "spring.data.elasticsearch.repositories.enabled", havingValue = "true", matchIfMissing = false)
public class ProductIndexingServiceImpl implements ProductIndexingService {

    private final ProductDocumentRepository productDocumentRepository;
    private final ProductRepository productRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    @Async
    @Transactional
    public void indexProduct(Product product) {
        try {
            log.info("Indexing product: {}", product.getProductId());
            ProductDocument document = toProductDocument(product);
            productDocumentRepository.save(document);
            log.info("Successfully indexed product: {}", product.getProductId());
        } catch (Exception e) {
            log.error("Error indexing product: {}", product.getProductId(), e);
            // Don't throw exception to avoid breaking the main flow
        }
    }

    @Override
    @Async
    public void deleteProduct(String productId) {
        try {
            log.info("Deleting product from index by productId: {}", productId);
            // Since productId is now the document ID, we can delete directly
            productDocumentRepository.deleteById(productId);
            log.info("Successfully deleted product from index: {}", productId);
        } catch (Exception e) {
            log.error("Error deleting product from index: {}", productId, e);
        }
    }

    @Override
    @Async
    public void deleteProductById(String mongoId) {
        try {
            log.info("Deleting product from index by MongoDB id: {}", mongoId);
            // Find the product by MongoDB _id, then delete by productId
            productRepository.findById(mongoId).ifPresent(product -> {
                productDocumentRepository.deleteById(product.getProductId());
                log.info("Successfully deleted product from index: {} (productId: {})", mongoId, product.getProductId());
            });
        } catch (Exception e) {
            log.error("Error deleting product from index: {}", mongoId, e);
        }
    }

    @Override
    @Async
    public void updateProduct(Product product) {
        // Update is same as index (save will update if exists)
        indexProduct(product);
    }

    @Override
    @Transactional(readOnly = true)
    public void reindexAllProducts() {
        log.info("Starting full reindex of all products");
        
        // Delete and recreate the index to ensure clean reindex with new document ID structure
        // This is necessary when changing document ID structure (from MongoDB _id to productId)
        try {
            log.info("Deleting and recreating Elasticsearch index to ensure clean reindex...");
            IndexOperations indexOps = elasticsearchOperations.indexOps(ProductDocument.class);
            
            if (indexOps.exists()) {
                log.info("Deleting existing index...");
                indexOps.delete();
                log.info("Index deleted. Waiting 2 seconds before recreation...");
                Thread.sleep(2000); // Wait for index deletion to complete
            }
            
            log.info("Creating new index with updated mapping...");
            indexOps.create();
            indexOps.putMapping(indexOps.createMapping(ProductDocument.class));
            log.info("Index recreated successfully. Starting fresh index...");
        } catch (Exception e) {
            log.warn("Error deleting/recreating index (will try to delete all documents instead): {}", e.getMessage());
            // Fallback: try to delete all documents
            try {
                log.info("Attempting to delete all existing documents...");
                productDocumentRepository.deleteAll();
                log.info("All existing documents deleted.");
            } catch (Exception e2) {
                log.error("Error deleting documents: {}", e2.getMessage());
                // Continue anyway - saveAll will handle duplicates
            }
        }
        
        int pageSize = 1000;
        int page = 0;
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Product> productPage;

        do {
            productPage = productRepository.findAll(pageable);
            List<ProductDocument> documents = productPage.getContent().stream()
                    .map(this::toProductDocument)
                    .collect(Collectors.toList());
            
            productDocumentRepository.saveAll(documents);
            log.info("Indexed page {} of products ({} total)", page, productPage.getTotalElements());
            
            page++;
            pageable = PageRequest.of(page, pageSize);
        } while (productPage.hasNext());

        log.info("Full reindex completed");
    }

    private ProductDocument toProductDocument(Product product) {
        List<VariantDocument> variantDocuments = product.getVariants().stream()
                .map(this::toVariantDocument)
                .collect(Collectors.toList());

        return ProductDocument.builder()
                .id(product.getProductId()) // Use productId as document ID
                .productId(product.getProductId())
                .name(product.getName())
                .description(product.getDescription())
                .category(product.getCategory())
                .brand(product.getBrand())
                .tags(product.getTags())
                .variants(variantDocuments)
                .build();
    }

    private VariantDocument toVariantDocument(Variant variant) {
        return VariantDocument.builder()
                .sku(variant.getSku())
                .color(variant.getColor())
                .size(variant.getSize())
                .price(variant.getPrice())
                .stock(variant.getStock())
                .build();
    }
}

