package com.blibli.SearchService.controller;

import com.blibli.SearchService.entity.ProductDocument;

import com.blibli.SearchService.service.ProductSearchService;
import com.blibli.SearchService.util.ApiResponse;
import com.blibli.SearchService.util.ResponseUtil;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
public class ProductSearchController {

    private final ProductSearchService service;

    public ProductSearchController(ProductSearchService service) {
        this.service = service;
    }

    @GetMapping("/name")
    public ResponseEntity<ApiResponse<Page<ProductDocument>>> searchByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ProductDocument> result = service.searchByName(name,page, size);
        return ResponseUtil.success("Products Fetched Successfully",result);
    }

    @GetMapping("/sku")
    public ResponseEntity<ApiResponse<ProductDocument>> searchBySku(
            @RequestParam String sku) {
        ProductDocument result = service.searchBySku(sku);
        return ResponseUtil.success("Products Fetched Successfully",result);
    }
}
