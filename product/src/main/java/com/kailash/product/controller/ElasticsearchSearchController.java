package com.kailash.product.controller;

import com.kailash.product.entity.Product;
import com.kailash.product.entity.ProductIndex;
import com.kailash.product.service.ProductIndexService;
import com.kailash.product.entity.ProductIndex;
import com.kailash.product.service.ProductIndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/es-search")
@RequiredArgsConstructor
public class ElasticsearchSearchController {

    @Autowired
    ProductIndexService productIndexService;

    @GetMapping
    public Iterable<ProductIndex> search(@RequestParam("q") String q) {
        return productIndexService.search(q);
    }

    @PostMapping("/reindex")
    public String reindexAll() {
        productIndexService.reindexAll();
        return "Reindexed all Mongo products into Elasticsearch";
    }
}
