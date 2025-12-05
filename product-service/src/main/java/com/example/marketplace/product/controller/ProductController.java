package com.example.marketplace.product.controller;

import com.example.marketplace.common.dto.ApiResponse;
import com.example.marketplace.product.domain.Product;
import com.example.marketplace.product.dto.ProductRequestDTO;
import com.example.marketplace.product.dto.ProductResponseDTO;
import com.example.marketplace.product.mapper.ProductMapper;
import com.example.marketplace.product.repo.ProductRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/internal/products")
public class ProductController {

    private final ProductRepository repo;

    public ProductController(ProductRepository repo) { this.repo = repo; }

    @PostMapping
    public ApiResponse<ProductResponseDTO> create(@Valid @RequestBody ProductRequestDTO req) {
        Product p = ProductMapper.toEntity(req);
        repo.save(p);
        return ApiResponse.ok(ProductMapper.toDto(p));
    }

    @GetMapping
    public ApiResponse<?> list(@RequestParam(value="q", required=false) String q,
                               @RequestParam(value="page", defaultValue="0") int page,
                               @RequestParam(value="size", defaultValue="20") int size) {
        String query = (q == null) ? "" : q;
        var pageReq = PageRequest.of(page, size);
        var pg = repo.findByNameContainingIgnoreCase(query, pageReq);
        List<ProductResponseDTO> items = pg.get()
                .map(ProductMapper::toDto)
                .collect(Collectors.toList());
        var resp = java.util.Map.of(
                "totalElements", pg.getTotalElements(),
                "totalPages", pg.getTotalPages(),
                "page", page,
                "size", size,
                "items", items
        );
        return ApiResponse.ok(resp);
    }

    @GetMapping("/{id}")
    public ApiResponse<?> detail(@PathVariable String id) {
        return repo.findById(id)
                .map(p -> ApiResponse.ok(ProductMapper.toDto(p)))
                .orElseGet(() -> ApiResponse.error("Product not found", 404));
    }
}
