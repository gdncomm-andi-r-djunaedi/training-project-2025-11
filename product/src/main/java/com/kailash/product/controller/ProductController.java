package com.kailash.product.controller;

import com.kailash.product.dto.ProductRequest;
import com.kailash.product.dto.ProductResponse;
import com.kailash.product.dto.ApiResponse;
import com.kailash.product.entity.Product;
import com.kailash.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    ProductService svc;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> create(@RequestBody ProductRequest req) {
        Product p = Product.of(req.getSku(), req.getName(), req.getShortDescription(), req.getPrice());
        Product saved = svc.create(p);

        ProductResponse resp = new ProductResponse(
                saved.getSku(),
                saved.getName(),
                saved.getShortDescription(),
                saved.getPrice()
        );

        return ResponseEntity
                .created(URI.create("/products/" + saved.getSku()))
                .body(ApiResponse.success(resp));
    }

    @PostMapping("/bulk")
    public ResponseEntity<?> bulkCreate(@RequestBody List<ProductRequest> reqs) {
        reqs.forEach(req -> {
            Product p = Product.of(
                    req.getSku(),
                    req.getName(),
                    req.getShortDescription(),
                    req.getPrice()
            );
            svc.create(p);
        });
        return ResponseEntity.ok("Inserted: " + reqs.size());
    }


    @GetMapping("/{sku}")
    public ResponseEntity<ApiResponse<ProductResponse>> get(@PathVariable("sku") String sku) {
        return svc.findBySku(sku)
                .map(p -> {
                    ProductResponse resp = new ProductResponse(
                            p.getSku(),
                            p.getName(),
                            p.getShortDescription(),
                            p.getPrice()
                    );
                    return ResponseEntity.ok(ApiResponse.success(resp));
                })
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.failure("Product not found")));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> list(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "search", required = false) String search
    ) {
        Page<Product> p = svc.list(search, page, size);

        List<ProductResponse> items = p.stream()
                .map(prod -> new ProductResponse(
                        prod.getSku(),
                        prod.getName(),
                        prod.getShortDescription(),
                        prod.getPrice()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @PutMapping("/{sku}")
    public ResponseEntity<ApiResponse<ProductResponse>> update(
            @PathVariable("sku") String sku,
            @RequestBody ProductRequest req
    ) {
        Product upd = Product.of(sku, req.getName(), req.getShortDescription(), req.getPrice());
        Product saved = svc.update(sku, upd);

        ProductResponse resp = new ProductResponse(
                saved.getSku(),
                saved.getName(),
                saved.getShortDescription(),
                saved.getPrice()
        );

        return ResponseEntity.ok(ApiResponse.success(resp));
    }

    @DeleteMapping("/{sku}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("sku") String sku) {
        svc.delete(sku);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
