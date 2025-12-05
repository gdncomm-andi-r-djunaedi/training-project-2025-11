package com.gdn.project.waroenk.catalog.controller.http;

import com.gdn.project.waroenk.catalog.AdjustStockRequest;
import com.gdn.project.waroenk.catalog.BulkAcquireStockRequest;
import com.gdn.project.waroenk.catalog.BulkAcquireStockResponse;
import com.gdn.project.waroenk.catalog.BulkAdjustStockRequest;
import com.gdn.project.waroenk.catalog.BulkAdjustStockResponse;
import com.gdn.project.waroenk.catalog.BulkLockStockRequest;
import com.gdn.project.waroenk.catalog.BulkLockStockResponse;
import com.gdn.project.waroenk.catalog.BulkReleaseStockRequest;
import com.gdn.project.waroenk.catalog.BulkReleaseStockResponse;
import com.gdn.project.waroenk.catalog.BulkUpdateStockResponse;
import com.gdn.project.waroenk.catalog.FilterInventoryRequest;
import com.gdn.project.waroenk.catalog.FindInventoryBySubSkuRequest;
import com.gdn.project.waroenk.catalog.InventoryData;
import com.gdn.project.waroenk.catalog.InventoryServiceGrpc;
import com.gdn.project.waroenk.catalog.MultipleInventoryResponse;
import com.gdn.project.waroenk.catalog.StockOperationItem;
import com.gdn.project.waroenk.catalog.UpdateStockBySubSkuRequest;
import com.gdn.project.waroenk.catalog.constant.ApiConstant;
import com.gdn.project.waroenk.catalog.dto.BasicDto;
import com.gdn.project.waroenk.catalog.dto.inventory.*;
import com.gdn.project.waroenk.catalog.mapper.InventoryMapper;
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
@RestController("catalogHttpInventoryController")
@Tag(name = "Inventory", description = "Inventory management API")
public class InventoryController {
  private static final InventoryMapper mapper = InventoryMapper.INSTANCE;
  private final InventoryServiceGrpc.InventoryServiceBlockingStub grpcClient;
  private final SearchService searchService;

  @Autowired
  public InventoryController(
      @GrpcClient("catalog-service") InventoryServiceGrpc.InventoryServiceBlockingStub grpcClient,
      SearchService searchService) {
    this.grpcClient = grpcClient;
    this.searchService = searchService;
  }

  @PostMapping("/inventory")
  public InventoryResponseDto createInventory(@Valid @RequestBody CreateInventoryRequestDto requestDto) {
    InventoryData response = grpcClient.createInventory(mapper.toRequestGrpc(requestDto));
    return mapper.toResponseDto(response);
  }

  @PutMapping("/inventory/{id}")
  public InventoryResponseDto updateInventory(@PathVariable String id,
      @RequestBody UpdateInventoryRequestDto requestDto) {
    InventoryData response = grpcClient.updateInventory(mapper.toRequestGrpc(id, requestDto));
    return mapper.toResponseDto(response);
  }

  @PutMapping("/inventory/by-sub-sku")
  public InventoryResponseDto updateStockBySubSku(@RequestParam String subSku, @RequestParam Long stock) {
    InventoryData response = grpcClient.updateStockBySubSku(
        UpdateStockBySubSkuRequest.newBuilder().setSubSku(subSku).setStock(stock).build());
    return mapper.toResponseDto(response);
  }

  @PatchMapping("/inventory/adjust")
  public InventoryResponseDto adjustStock(@RequestParam String subSku, @RequestParam Long quantity) {
    InventoryData response = grpcClient.adjustStock(
        AdjustStockRequest.newBuilder().setSubSku(subSku).setQuantity(quantity).build());
    return mapper.toResponseDto(response);
  }

  @PutMapping("/inventory/bulk")
  public BulkUpdateStockResponseDto bulkUpdateStock(@Valid @RequestBody BulkUpdateStockRequestDto requestDto) {
    BulkUpdateStockResponse response = grpcClient.bulkUpdateStock(mapper.toRequestGrpc(requestDto));
    return mapper.toResponseDto(response);
  }

  @DeleteMapping("/inventory/{id}")
  public BasicDto deleteInventory(@PathVariable String id) {
    Basic response = grpcClient.deleteInventory(Id.newBuilder().setValue(id).build());
    return mapper.toBasicDto(response);
  }

  @GetMapping("/inventory/{id}")
  public InventoryResponseDto findInventoryById(@PathVariable String id) {
    InventoryData response = grpcClient.findInventoryById(Id.newBuilder().setValue(id).build());
    return mapper.toResponseDto(response);
  }

  @GetMapping("/inventory/by-sub-sku")
  public InventoryResponseDto findInventoryBySubSku(@RequestParam String subSku) {
    InventoryData response = grpcClient.findInventoryBySubSku(
        FindInventoryBySubSkuRequest.newBuilder().setSubSku(subSku).build());
    return mapper.toResponseDto(response);
  }

  @GetMapping("/inventory/filter")
  public ListOfInventoryResponseDto filterInventory(
      @RequestParam(required = false) String subSku,
      @RequestParam(required = false) Long minStock,
      @RequestParam(required = false) Long maxStock,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false, defaultValue = "id") String sortBy,
      @RequestParam(required = false, defaultValue = "asc") String sortDirection) {
    FilterInventoryRequest.Builder builder = FilterInventoryRequest.newBuilder().setSize(size);
    if (StringUtils.isNotBlank(subSku)) builder.setSubSku(subSku);
    if (minStock != null) builder.setMinStock(minStock);
    if (maxStock != null) builder.setMaxStock(maxStock);
    if (StringUtils.isNotBlank(cursor)) builder.setCursor(cursor);
    builder.setSortBy(SortBy.newBuilder().setField(sortBy).setDirection(sortDirection).build());

    MultipleInventoryResponse response = grpcClient.filterInventory(builder.build());
    return mapper.toResponseDto(response);
  }

  @PostMapping("/inventory/check")
  @Operation(summary = "Check inventory for multiple subSkus",
      description = "Returns stock info with hasStock boolean for each requested subSku. Uses short TTL caching (30 seconds).")
  public ResponseEntity<InventoryCheckResponseDto> checkInventory(
      @Valid @RequestBody InventoryCheckRequestDto request) {
    try {
      SearchService.InventoryCheckResult result = searchService.checkInventory(request.subSkus());
      return ResponseEntity.ok(new InventoryCheckResponseDto(
          result.items(),
          result.totalFound(),
          result.totalRequested(),
          result.took()
      ));
    } catch (Exception e) {
      log.error("Error checking inventory", e);
      throw new RuntimeException("Failed to check inventory: " + e.getMessage(), e);
    }
  }

  // ============================================================
  // Bulk Lock/Acquire/Release Operations for Cart/Checkout
  // ============================================================

  @PostMapping("/inventory/bulk-lock")
  @Operation(summary = "Bulk lock stock for checkout",
      description = "Reserve inventory for checkout. Creates a temporary lock with TTL. Stock is not actually reduced until acquired.")
  public BulkStockOperationResponseDto bulkLockStock(@Valid @RequestBody BulkLockStockRequestDto requestDto) {
    BulkLockStockRequest.Builder builder = BulkLockStockRequest.newBuilder()
        .setCheckoutId(requestDto.checkoutId());

    if (requestDto.lockTtlSeconds() != null) {
      builder.setLockTtlSeconds(requestDto.lockTtlSeconds());
    }

    requestDto.items().forEach(item -> builder.addItems(
        StockOperationItem.newBuilder()
            .setSubSku(item.subSku())
            .setQuantity(item.quantity())
            .build()
    ));

    BulkLockStockResponse response = grpcClient.bulkLockStock(builder.build());
    return mapToStockOperationResponse(response.getCheckoutId(), response.getResultsList(),
        response.getAllSuccess(), response.getSuccessCount(), response.getFailureCount());
  }

  @PostMapping("/inventory/bulk-acquire")
  @Operation(summary = "Bulk acquire stock",
      description = "Confirm reservation and reduce stock. Should be called after successful payment to finalize the stock reduction.")
  public BulkStockOperationResponseDto bulkAcquireStock(@Valid @RequestBody BulkAcquireStockRequestDto requestDto) {
    BulkAcquireStockRequest.Builder builder = BulkAcquireStockRequest.newBuilder()
        .setCheckoutId(requestDto.checkoutId());

    requestDto.items().forEach(item -> builder.addItems(
        StockOperationItem.newBuilder()
            .setSubSku(item.subSku())
            .setQuantity(item.quantity())
            .build()
    ));

    BulkAcquireStockResponse response = grpcClient.bulkAcquireStock(builder.build());
    return mapToStockOperationResponse(response.getCheckoutId(), response.getResultsList(),
        response.getAllSuccess(), response.getSuccessCount(), response.getFailureCount());
  }

  @PostMapping("/inventory/bulk-release")
  @Operation(summary = "Bulk release stock",
      description = "Return reserved stock. Should be called when checkout is cancelled or expired.")
  public BulkStockOperationResponseDto bulkReleaseStock(@Valid @RequestBody BulkReleaseStockRequestDto requestDto) {
    BulkReleaseStockRequest.Builder builder = BulkReleaseStockRequest.newBuilder()
        .setCheckoutId(requestDto.checkoutId());

    requestDto.items().forEach(item -> builder.addItems(
        StockOperationItem.newBuilder()
            .setSubSku(item.subSku())
            .setQuantity(item.quantity())
            .build()
    ));

    BulkReleaseStockResponse response = grpcClient.bulkReleaseStock(builder.build());
    return mapToStockOperationResponse(response.getCheckoutId(), response.getResultsList(),
        response.getAllSuccess(), response.getSuccessCount(), response.getFailureCount());
  }

  @PostMapping("/inventory/bulk-adjust")
  @Operation(summary = "Bulk adjust stock",
      description = "Adjust stock by quantity (positive or negative) for multiple items.")
  public BulkAdjustStockResponseDto bulkAdjustStock(@Valid @RequestBody BulkAdjustStockRequestDto requestDto) {
    BulkAdjustStockRequest.Builder builder = BulkAdjustStockRequest.newBuilder();

    requestDto.items().forEach(item -> builder.addItems(
        com.gdn.project.waroenk.catalog.AdjustStockRequest.newBuilder()
            .setSubSku(item.subSku())
            .setQuantity(item.quantity())
            .build()
    ));

    BulkAdjustStockResponse response = grpcClient.bulkAdjustStock(builder.build());
    return mapToBulkAdjustResponse(response);
  }

  // Helper methods for mapping responses

  private BulkStockOperationResponseDto mapToStockOperationResponse(
      String checkoutId,
      java.util.List<com.gdn.project.waroenk.catalog.StockOperationResult> results,
      boolean allSuccess,
      int successCount,
      int failureCount) {
    return new BulkStockOperationResponseDto(
        checkoutId,
        results.stream().map(r -> new StockOperationResultDto(
            r.getSubSku(),
            r.getSuccess(),
            r.getMessage(),
            r.getCurrentStock(),
            r.getRequestedQuantity()
        )).toList(),
        allSuccess,
        successCount,
        failureCount
    );
  }

  private BulkAdjustStockResponseDto mapToBulkAdjustResponse(BulkAdjustStockResponse response) {
    return new BulkAdjustStockResponseDto(
        response.getResultsList().stream().map(r -> new StockOperationResultDto(
            r.getSubSku(),
            r.getSuccess(),
            r.getMessage(),
            r.getCurrentStock(),
            r.getRequestedQuantity()
        )).toList(),
        response.getAllSuccess(),
        response.getSuccessCount(),
        response.getFailureCount()
    );
  }
}




