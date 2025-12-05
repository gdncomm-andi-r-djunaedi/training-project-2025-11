package com.blibli.gdn.productService.config;

import com.blibli.gdn.productService.repository.ProductDocumentRepository;
import com.blibli.gdn.productService.repository.ProductRepository;
import com.blibli.gdn.productService.service.ProductIndexingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Configuration for automatic Elasticsearch synchronization
 * - Checks sync status on startup
 * - Performs incremental sync if needed
 * - Scheduled sync as fallback
 */
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "spring.data.elasticsearch.repositories.enabled", havingValue = "true", matchIfMissing = false)
public class ElasticsearchSyncConfig implements ApplicationListener<ApplicationReadyEvent> {

    private final ProductRepository productRepository;
    private final ProductDocumentRepository productDocumentRepository;
    private final ProductIndexingService productIndexingService;

    /**
     * Check and sync Elasticsearch on application startup
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("Checking Elasticsearch sync status...");
        
        try {
            long mongoCount = productRepository.count();
            long esCount = productDocumentRepository.count();
            
            log.info("MongoDB products: {}, Elasticsearch documents: {}", mongoCount, esCount);
            
            if (mongoCount == 0) {
                log.info("No products in MongoDB, skipping sync");
                return;
            }
            
            // If counts differ significantly (more than 5%), trigger reindex
            double difference = Math.abs(mongoCount - esCount);
            double percentage = (difference / mongoCount) * 100;
            
            if (percentage > 5.0 || esCount == 0) {
                log.warn("Elasticsearch is out of sync ({}% difference). Starting full reindex...", 
                        String.format("%.2f", percentage));
                
                // Run reindex asynchronously to not block startup
                new Thread(() -> {
                    try {
                        Thread.sleep(5000); // Wait 5 seconds for application to fully start
                        productIndexingService.reindexAllProducts();
                        log.info("Startup reindex completed");
                    } catch (Exception e) {
                        log.error("Error during startup reindex: {}", e.getMessage(), e);
                    }
                }).start();
            } else {
                log.info("Elasticsearch is in sync (difference: {}%)", String.format("%.2f", percentage));
            }
        } catch (Exception e) {
            log.error("Error checking Elasticsearch sync status: {}", e.getMessage(), e);
        }
    }

    /**
     * Scheduled sync job - runs every hour as a fallback
     * This ensures Elasticsearch stays in sync even if some events are missed
     */
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    public void scheduledSync() {
        log.info("Running scheduled Elasticsearch sync check...");
        
        try {
            long mongoCount = productRepository.count();
            long esCount = productDocumentRepository.count();
            
            double difference = Math.abs(mongoCount - esCount);
            double percentage = (difference / mongoCount) * 100;
            
            if (percentage > 10.0) { // Only reindex if difference is more than 10%
                log.warn("Scheduled sync detected {}% difference. Starting reindex...", 
                        String.format("%.2f", percentage));
                productIndexingService.reindexAllProducts();
            } else {
                log.debug("Scheduled sync check: Elasticsearch is in sync (difference: {}%)", 
                        String.format("%.2f", percentage));
            }
        } catch (Exception e) {
            log.error("Error during scheduled sync: {}", e.getMessage(), e);
        }
    }
}

