package com.kailash.product.repository;

import com.kailash.product.entity.Product;
import com.kailash.product.entity.ProductIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository("productElasticSearchRepository")
public interface ProductElasticSearchRepository extends ElasticsearchRepository<ProductIndex,String> {
    List<ProductIndex> findByNameContainingIgnoreCase(String name);

    List<ProductIndex> findByShortDescriptionContainingIgnoreCase(String text);

    List<ProductIndex> findBySku(String sku);
}
