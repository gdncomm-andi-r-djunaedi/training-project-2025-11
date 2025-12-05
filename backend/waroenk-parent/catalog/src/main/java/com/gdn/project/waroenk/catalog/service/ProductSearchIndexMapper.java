package com.gdn.project.waroenk.catalog.service;

import com.gdn.project.waroenk.catalog.dto.product.AggregatedProductDto;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class ProductSearchIndexMapper implements SearchIndexMapper<AggregatedProductDto> {

  @Override
  public String id(AggregatedProductDto entity) {
    return entity.subSku();
  }

  @Override
  public String type() {
    return "products";
  }

  @Override
  public String title(AggregatedProductDto entity) {
    return entity.title();
  }

  @Override
  public String body(AggregatedProductDto entity) {
    StringBuilder body = new StringBuilder();
    if (entity.summary() != null) {
      body.append(entity.summary()).append(" ");
    }
    if (entity.brand() != null) {
      body.append(entity.brand()).append(" ");
    }
    if (entity.category() != null) {
      body.append(entity.category()).append(" ");
    }
    if (entity.merchantName() != null) {
      body.append(entity.merchantName()).append(" ");
    }
    if (entity.variantKeywords() != null) {
      body.append(String.join(" ", entity.variantKeywords()));
    }
    return body.toString().trim();
  }

  @Override
  public Map<String, Object> extraFields(AggregatedProductDto entity) {
    Map<String, Object> fields = new HashMap<>();
    fields.put("sku", entity.sku());
    fields.put("slug", entity.slug());
    fields.put("brand", entity.brand());
    fields.put("price", entity.price());
    fields.put("subSku", entity.subSku());
    fields.put("inStock", entity.inStock());
    fields.put("category", entity.category());
    fields.put("thumbnail", entity.thumbnail());
    fields.put("attributes", entity.attributes());
    fields.put("merchantName", entity.merchantName());
    fields.put("variantKeywords", entity.variantKeywords());
    fields.put("merchantLocation", entity.merchantLocation());
    return fields;
  }

  @Override
  public Set<String> queryAbleFields() {
    // Order matters for Typesense performance - most important fields first
    // Using LinkedHashSet to preserve order
    return new java.util.LinkedHashSet<>(java.util.List.of(
        "title",           // Product title - most searched
        "body",            // Contains summary, brand, category, merchant, keywords
        "brand",           // Brand name
        "category",        // Category name
        "merchantName",    // Merchant name
        "sku",             // Product SKU
        "subSku"           // Variant SKU
    ));
  }
}
