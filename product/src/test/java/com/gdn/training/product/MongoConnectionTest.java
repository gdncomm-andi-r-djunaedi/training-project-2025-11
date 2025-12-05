package com.gdn.training.product;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class MongoConnectionTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void testConnection() {
        assertThat(mongoTemplate.getDb()).isNotNull();
        System.out.println("MongoDB connection successful! Database name: " + mongoTemplate.getDb().getName());

        // Try to list collection names to ensure we can talk to the DB
        mongoTemplate.getCollectionNames().forEach(name -> System.out.println("Collection: " + name));
    }
}
