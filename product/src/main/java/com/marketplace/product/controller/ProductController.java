package com.marketplace.product.controller;

import com.marketplace.common.dto.ApiResponse;
import com.marketplace.common.dto.PageInfo;
import com.marketplace.product.dto.ProductResponse;
import com.marketplace.product.dto.ProductSearchRequest;
import com.marketplace.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product", description = "Product catalog APIs - list, search, dan detail produk")
public class ProductController {

    private final ProductService productService;

    @Operation(
            summary = "List semua produk",
            description = "Mengambil daftar produk dengan pagination. Bisa diurutkan berdasarkan field tertentu."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Berhasil mendapatkan list produk",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> listProducts(
            @Parameter(description = "Nomor halaman (dimulai dari 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Jumlah item per halaman", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field untuk sorting (name, price, createdAt)", example = "price")
            @RequestParam(required = false) String sortBy,
            @Parameter(description = "Arah sorting (asc, desc)", example = "asc")
            @RequestParam(required = false) String sortDirection) {
        
        log.info("List products request - page: {}, size: {}", page, size);
        
        Page<ProductResponse> productPage = productService.listProducts(page, size, sortBy, sortDirection);
        PageInfo pageInfo = productService.createPageInfo(productPage);
        
        return ResponseEntity.ok(ApiResponse.success(productPage.getContent(), pageInfo));
    }

    @Operation(
            summary = "Search produk",
            description = "Mencari produk berdasarkan keyword, kategori, dan brand. Mendukung pagination dan sorting."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Berhasil mencari produk")
    })
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> searchProducts(
            @Parameter(description = "Keyword pencarian (nama atau deskripsi)", example = "laptop")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Filter berdasarkan kategori", example = "Electronics")
            @RequestParam(required = false) String category,
            @Parameter(description = "Filter berdasarkan brand", example = "Apple")
            @RequestParam(required = false) String brand,
            @Parameter(description = "Nomor halaman", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Jumlah item per halaman", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field untuk sorting", example = "price")
            @RequestParam(required = false) String sortBy,
            @Parameter(description = "Arah sorting (asc, desc)", example = "desc")
            @RequestParam(required = false) String sortDirection) {
        
        log.info("Search products request - keyword: {}, category: {}, brand: {}", keyword, category, brand);
        
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword(keyword)
                .category(category)
                .brand(brand)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
        
        Page<ProductResponse> productPage = productService.searchProducts(request);
        PageInfo pageInfo = productService.createPageInfo(productPage);
        
        return ResponseEntity.ok(ApiResponse.success(productPage.getContent(), pageInfo));
    }

    @Operation(
            summary = "Get produk by ID",
            description = "Mengambil detail produk berdasarkan ID"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Berhasil mendapatkan detail produk"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Produk tidak ditemukan")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(
            @Parameter(description = "Product ID (MongoDB ObjectId)", required = true, example = "507f1f77bcf86cd799439011")
            @PathVariable String id) {
        log.info("Get product by id request: {}", id);
        ProductResponse response = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "Get multiple produk by IDs",
            description = "Mengambil detail beberapa produk sekaligus berdasarkan list ID. Berguna untuk cart service."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Berhasil mendapatkan list produk")
    })
    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductsByIds(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "List of product IDs",
                    content = @Content(schema = @Schema(type = "array", example = "[\"507f1f77bcf86cd799439011\", \"507f1f77bcf86cd799439012\"]")))
            @RequestBody List<String> ids) {
        log.info("Get products by ids request: {}", ids);
        List<ProductResponse> responses = productService.getProductsByIds(ids);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
