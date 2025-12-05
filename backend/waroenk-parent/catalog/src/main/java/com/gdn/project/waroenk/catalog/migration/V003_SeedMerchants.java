package com.gdn.project.waroenk.catalog.migration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdn.project.waroenk.catalog.entity.Merchant;
import com.gdn.project.waroenk.catalog.entity.SeedChecksum;
import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Migration V003: Seed initial merchants data.
 */
@Slf4j
@ChangeLog(order = "003")
public class V003_SeedMerchants {

  private static final String MERCHANTS_FILE = "seed-data/merchants.json";
  private final ObjectMapper objectMapper = new ObjectMapper();

  @ChangeSet(order = "001", id = "seedMerchants", author = "system")
  public void seedMerchants(MongockTemplate mongockTemplate) {
    log.info("Starting merchant seeding...");
    
    try {
      ClassPathResource resource = new ClassPathResource(MERCHANTS_FILE);
      if (!resource.exists()) {
        log.warn("Merchants seed file not found: {}. Skipping merchant seeding.", MERCHANTS_FILE);
        return;
      }

      String content;
      try (InputStream is = resource.getInputStream()) {
        content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
      }
      
      String checksum = calculateChecksum(content);
      
      if (isAlreadyProcessed(mongockTemplate, MERCHANTS_FILE, checksum)) {
        log.info("Merchants file already processed with same checksum. Skipping.");
        return;
      }
      
      // Parse raw JSON to handle nested contact object
      List<Map<String, Object>> merchantsRaw = objectMapper.readValue(
          content, 
          new TypeReference<List<Map<String, Object>>>() {}
      );
      
      Instant now = Instant.now();
      
      mongockTemplate.dropCollection(Merchant.class);
      
      for (Map<String, Object> rawMerchant : merchantsRaw) {
        @SuppressWarnings("unchecked")
        Map<String, String> contactMap = (Map<String, String>) rawMerchant.get("contact");
        
        Merchant.ContactInfo contactInfo = null;
        if (contactMap != null) {
          contactInfo = Merchant.ContactInfo.builder()
              .phone(contactMap.get("phone"))
              .email(contactMap.get("email"))
              .build();
        }
        
        Number ratingNum = (Number) rawMerchant.get("rating");
        
        Merchant merchant = Merchant.builder()
            .id((String) rawMerchant.get("id"))
            .name((String) rawMerchant.get("name"))
            .code((String) rawMerchant.get("code"))
            .iconUrl((String) rawMerchant.get("iconUrl"))
            .location((String) rawMerchant.get("location"))
            .contact(contactInfo)
            .rating(ratingNum != null ? ratingNum.floatValue() : null)
            .createdAt(now)
            .updatedAt(now)
            .build();
        
        mongockTemplate.insert(merchant);
      }
      
      recordChecksum(mongockTemplate, MERCHANTS_FILE, checksum, merchantsRaw.size());
      
      log.info("Successfully seeded {} merchants", merchantsRaw.size());
      
    } catch (IOException e) {
      log.error("Failed to seed merchants: {}", e.getMessage(), e);
    }
  }

  private String calculateChecksum(String content) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("SHA-256 algorithm not available", e);
    }
  }

  private boolean isAlreadyProcessed(MongockTemplate mongockTemplate, String fileName, String checksum) {
    Query query = Query.query(
        Criteria.where("fileName").is(fileName)
            .and("checksum").is(checksum)
    );
    return mongockTemplate.exists(query, SeedChecksum.class);
  }

  private void recordChecksum(MongockTemplate mongockTemplate, String fileName, String checksum, int recordCount) {
    Query deleteQuery = Query.query(Criteria.where("fileName").is(fileName));
    mongockTemplate.remove(deleteQuery, SeedChecksum.class);
    
    SeedChecksum seedChecksum = SeedChecksum.builder()
        .id(UUID.randomUUID().toString())
        .fileName(fileName)
        .checksum(checksum)
        .recordCount(recordCount)
        .processedAt(Instant.now())
        .build();
    mongockTemplate.insert(seedChecksum);
  }
}
