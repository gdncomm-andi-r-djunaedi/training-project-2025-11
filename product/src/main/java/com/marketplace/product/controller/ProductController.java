package com.marketplace.product.controller;

import com.marketplace.common.controller.BaseCommandController;
import com.marketplace.common.dto.ApiResponse;
import com.marketplace.common.mapper.MapperService;
import com.marketplace.product.command.GetProductByIdCommand;
import com.marketplace.product.command.SearchProductsCommand;
import com.marketplace.product.command.SeedProductsCommand;
import com.marketplace.product.document.Product;
import com.marketplace.product.dto.request.GetProductByIdRequest;
import com.marketplace.product.dto.request.SearchProductsRequest;
import com.marketplace.product.dto.response.ProductResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for product operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/product")
public class ProductController extends BaseCommandController {

    @Autowired
    private MapperService mapperService;

    @PostMapping("/seed")
    public ResponseEntity<ApiResponse<String>> seedProducts() {
        log.info("Seed products request received");
        execute(SeedProductsCommand.class, null);
        return okResponse("Products seeded successfully", "Database populated");
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> searchProducts(
            @RequestParam(required = false, defaultValue = "") String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Search products request - name: '{}', page: {}, size: {}", name, page, size);

        Pageable pageable = PageRequest.of(page, size);
        SearchProductsRequest request = SearchProductsRequest.builder()
                .name(name)
                .pageable(pageable)
                .build();

        Page<Product> products = execute(SearchProductsCommand.class, request);
        Page<ProductResponse> response = products.map(p -> mapperService.map(p, ProductResponse.class));
        return okResponse(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable String id) {
        log.info("Get product request for ID: {}", id);

        GetProductByIdRequest request = GetProductByIdRequest.builder().productId(id).build();
        Product product = execute(GetProductByIdCommand.class, request);
        ProductResponse response = mapperService.map(product, ProductResponse.class);
        return okResponse(response);
    }
}
