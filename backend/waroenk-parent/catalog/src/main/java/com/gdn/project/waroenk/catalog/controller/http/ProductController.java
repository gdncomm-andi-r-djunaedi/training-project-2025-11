package com.gdn.project.waroenk.catalog.controller.http;

import com.gdn.project.waroenk.catalog.FilterProductRequest;
import com.gdn.project.waroenk.catalog.FindProductBySkuRequest;
import com.gdn.project.waroenk.catalog.MultipleProductResponse;
import com.gdn.project.waroenk.catalog.ProductData;
import com.gdn.project.waroenk.catalog.ProductServiceGrpc;
import com.gdn.project.waroenk.catalog.constant.ApiConstant;
import com.gdn.project.waroenk.catalog.dto.BasicDto;
import com.gdn.project.waroenk.catalog.dto.product.*;
import com.gdn.project.waroenk.catalog.mapper.ProductMapper;
import com.gdn.project.waroenk.catalog.service.SearchService;
import com.gdn.project.waroenk.common.Basic;
import com.gdn.project.waroenk.common.Id;
import com.gdn.project.waroenk.common.SortBy;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping(ApiConstant.BASE_PATH)
@RestController("catalogHttpProductController")
@Tag(name = "Product", description = "Product management API")
public class ProductController {
  private static final ProductMapper mapper = ProductMapper.INSTANCE;
  private final ProductServiceGrpc.ProductServiceBlockingStub grpcClient;
  private final SearchService searchService;

  @Autowired
  public ProductController(
      @GrpcClient("catalog-service") ProductServiceGrpc.ProductServiceBlockingStub grpcClient,
      SearchService searchService) {
    this.grpcClient = grpcClient;
    this.searchService = searchService;
  }

  @PostMapping("/product")
  public ProductResponseDto createProduct(@Valid @RequestBody CreateProductRequestDto requestDto) {
    ProductData response = grpcClient.createProduct(mapper.toRequestGrpc(requestDto));
    return mapper.toResponseDto(response);
  }

  @PutMapping("/product/{id}")
  public ProductResponseDto updateProduct(@PathVariable String id, @RequestBody UpdateProductRequestDto requestDto) {
    ProductData response = grpcClient.updateProduct(mapper.toRequestGrpc(id, requestDto));
    return mapper.toResponseDto(response);
  }

  @DeleteMapping("/product/{id}")
  public BasicDto deleteProduct(@PathVariable String id) {
    Basic response = grpcClient.deleteProduct(Id.newBuilder().setValue(id).build());
    return mapper.toBasicDto(response);
  }

  @GetMapping("/product/{id}")
  public ProductResponseDto findProductById(@PathVariable String id) {
    ProductData response = grpcClient.findProductById(Id.newBuilder().setValue(id).build());
    return mapper.toResponseDto(response);
  }

  @GetMapping("/product/by-sku")
  public ProductResponseDto findProductBySku(@RequestParam String sku) {
    ProductData response = grpcClient.findProductBySku(FindProductBySkuRequest.newBuilder().setSku(sku).build());
    return mapper.toResponseDto(response);
  }

  @GetMapping("/product/filter")
  public ListOfProductResponseDto filterProducts(
      @RequestParam(required = false) String title,
      @RequestParam(required = false) String sku,
      @RequestParam(required = false) String merchantCode,
      @RequestParam(required = false) String categoryId,
      @RequestParam(required = false) String brandId,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false, defaultValue = "id") String sortBy,
      @RequestParam(required = false, defaultValue = "asc") String sortDirection) {
    FilterProductRequest.Builder builder = FilterProductRequest.newBuilder().setSize(size);
    if (StringUtils.isNotBlank(title)) builder.setTitle(title);
    if (StringUtils.isNotBlank(sku)) builder.setSku(sku);
    if (StringUtils.isNotBlank(merchantCode)) builder.setMerchantCode(merchantCode);
    if (StringUtils.isNotBlank(categoryId)) builder.setCategoryId(categoryId);
    if (StringUtils.isNotBlank(brandId)) builder.setBrandId(brandId);
    if (StringUtils.isNotBlank(cursor)) builder.setCursor(cursor);
    builder.setSortBy(SortBy.newBuilder().setField(sortBy).setDirection(sortDirection).build());
    
    MultipleProductResponse response = grpcClient.filterProduct(builder.build());
    return mapper.toResponseDto(response);
  }

  // ============================================================
  // Product Details and Summary Endpoints (from Search)
  // ============================================================

  @GetMapping("/product/details")
  @Operation(summary = "Get verbose product details by ID (subSku or sku)",
      description = "Returns complete product info with merchant, brand, category, inventory, and variant images. " +
          "Uses caching: merchant/brand/category (1 hour TTL), inventory (30 seconds TTL).")
  public ResponseEntity<ProductDetailsResponse> getProductDetails(@RequestParam String id) {
    try {
      SearchService.ProductDetailsResult result = searchService.getProductDetails(id);
      return ResponseEntity.ok(new ProductDetailsResponse(result.product(), result.took()));
    } catch (Exception e) {
      log.error("Error getting product details for id: {}", id, e);
      throw new RuntimeException("Failed to get product details: " + e.getMessage(), e);
    }
  }

  @PostMapping("/product/summary")
  @Operation(summary = "Get product summary by multiple subSkus",
      description = "Batch lookup products by exact subSku match. Returns matching products with basic info.")
  public ResponseEntity<ProductSummaryResponseDto> getProductSummary(
      @Valid @RequestBody ProductSummaryRequestDto request) {
    try {
      SearchService.ProductSummaryResult result = searchService.getProductSummary(request.subSkus());
      return ResponseEntity.ok(new ProductSummaryResponseDto(
          result.products(),
          result.totalFound(),
          result.totalRequested(),
          result.took()
      ));
    } catch (Exception e) {
      log.error("Error getting product summary", e);
      throw new RuntimeException("Failed to get product summary: " + e.getMessage(), e);
    }
  }

  // Response wrapper for product details
  public record ProductDetailsResponse(ProductDetailDto product, long took) {}
}
