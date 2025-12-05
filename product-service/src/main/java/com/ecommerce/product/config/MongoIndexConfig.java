package com.ecommerce.product.config;

import com.ecommerce.product.entity.Product;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;

@Configuration
public class MongoIndexConfig {

    @Autowired
    private MongoTemplate mongoTemplate;

    @PostConstruct
    public void createIndexes() {
        // Create text index for search functionality
        TextIndexDefinition textIndex = TextIndexDefinition.builder()
                .onField("name")
                .onField("description")
                .build();

        mongoTemplate.indexOps(Product.class).ensureIndex(textIndex);

        // Create unique index on SKU
        Index skuIndex = new Index().on("sku", Sort.Direction.ASC).unique();
        mongoTemplate.indexOps(Product.class).ensureIndex(skuIndex);

        System.out.println("MongoDB indexes created successfully");
    }
}
