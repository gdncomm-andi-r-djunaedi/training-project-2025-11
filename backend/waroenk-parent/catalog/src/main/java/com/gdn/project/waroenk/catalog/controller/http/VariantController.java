package com.gdn.project.waroenk.catalog.controller.http;

import com.gdn.project.waroenk.catalog.FilterVariantRequest;
import com.gdn.project.waroenk.catalog.FindVariantBySubSkuRequest;
import com.gdn.project.waroenk.catalog.FindVariantsBySkuRequest;
import com.gdn.project.waroenk.catalog.MultipleVariantResponse;
import com.gdn.project.waroenk.catalog.SetDefaultVariantRequest;
import com.gdn.project.waroenk.catalog.VariantData;
import com.gdn.project.waroenk.catalog.VariantServiceGrpc;
import com.gdn.project.waroenk.catalog.constant.ApiConstant;
import com.gdn.project.waroenk.catalog.dto.BasicDto;
import com.gdn.project.waroenk.catalog.dto.variant.CreateVariantRequestDto;
import com.gdn.project.waroenk.catalog.dto.variant.ListOfVariantResponseDto;
import com.gdn.project.waroenk.catalog.dto.variant.UpdateVariantRequestDto;
import com.gdn.project.waroenk.catalog.dto.variant.VariantResponseDto;
import com.gdn.project.waroenk.catalog.mapper.VariantMapper;
import com.gdn.project.waroenk.common.Basic;
import com.gdn.project.waroenk.common.Id;
import com.gdn.project.waroenk.common.SortBy;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping(ApiConstant.BASE_PATH)
@RestController("catalogHttpVariantController")
@Tag(name = "Variant", description = "Variant management API")
public class VariantController {
  private static final VariantMapper mapper = VariantMapper.INSTANCE;
  private final VariantServiceGrpc.VariantServiceBlockingStub grpcClient;

  @Autowired
  public VariantController(@GrpcClient("catalog-service") VariantServiceGrpc.VariantServiceBlockingStub grpcClient) {
    this.grpcClient = grpcClient;
  }

  @PostMapping("/variant")
  @Operation(summary = "Create a new variant")
  public VariantResponseDto createVariant(@Valid @RequestBody CreateVariantRequestDto requestDto) {
    VariantData response = grpcClient.createVariant(mapper.toRequestGrpc(requestDto));
    return mapper.toResponseDto(response);
  }

  @PutMapping("/variant/{id}")
  @Operation(summary = "Update an existing variant")
  public VariantResponseDto updateVariant(@PathVariable String id, @RequestBody UpdateVariantRequestDto requestDto) {
    VariantData response = grpcClient.updateVariant(mapper.toRequestGrpc(id, requestDto));
    return mapper.toResponseDto(response);
  }

  @DeleteMapping("/variant/{id}")
  @Operation(summary = "Delete a variant")
  public BasicDto deleteVariant(@PathVariable String id) {
    Basic response = grpcClient.deleteVariant(Id.newBuilder().setValue(id).build());
    return mapper.toBasicDto(response);
  }

  @GetMapping("/variant/{id}")
  @Operation(summary = "Find variant by ID")
  public VariantResponseDto findVariantById(@PathVariable String id) {
    VariantData response = grpcClient.findVariantById(Id.newBuilder().setValue(id).build());
    return mapper.toResponseDto(response);
  }

  @GetMapping("/variant/by-sub-sku")
  @Operation(summary = "Find variant by sub-SKU")
  public VariantResponseDto findVariantBySubSku(@RequestParam String subSku) {
    VariantData response = grpcClient.findVariantBySubSku(
        FindVariantBySubSkuRequest.newBuilder().setSubSku(subSku).build());
    return mapper.toResponseDto(response);
  }

  @GetMapping("/variant/by-sku")
  @Operation(summary = "Find all variants by product SKU")
  public ListOfVariantResponseDto findVariantsBySku(
      @RequestParam String sku,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false, defaultValue = "id") String sortBy,
      @RequestParam(required = false, defaultValue = "asc") String sortDirection) {
    FindVariantsBySkuRequest.Builder builder = FindVariantsBySkuRequest.newBuilder()
        .setSku(sku)
        .setSize(size);
    if (StringUtils.isNotBlank(cursor)) builder.setCursor(cursor);
    builder.setSortBy(SortBy.newBuilder().setField(sortBy).setDirection(sortDirection).build());

    MultipleVariantResponse response = grpcClient.findVariantsBySku(builder.build());
    return mapper.toResponseDto(response);
  }

  @GetMapping("/variant/filter")
  @Operation(summary = "Filter variants with various criteria")
  public ListOfVariantResponseDto filterVariants(
      @RequestParam(required = false) String sku,
      @RequestParam(required = false) String subSku,
      @RequestParam(required = false) Double minPrice,
      @RequestParam(required = false) Double maxPrice,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false, defaultValue = "id") String sortBy,
      @RequestParam(required = false, defaultValue = "asc") String sortDirection) {
    FilterVariantRequest.Builder builder = FilterVariantRequest.newBuilder().setSize(size);
    if (StringUtils.isNotBlank(sku)) builder.setSku(sku);
    if (StringUtils.isNotBlank(subSku)) builder.setSubSku(subSku);
    if (minPrice != null) builder.setMinPrice(minPrice);
    if (maxPrice != null) builder.setMaxPrice(maxPrice);
    if (StringUtils.isNotBlank(cursor)) builder.setCursor(cursor);
    builder.setSortBy(SortBy.newBuilder().setField(sortBy).setDirection(sortDirection).build());

    MultipleVariantResponse response = grpcClient.filterVariant(builder.build());
    return mapper.toResponseDto(response);
  }

  @PutMapping("/variant/{id}/set-default")
  @Operation(summary = "Set a variant as the default variant for its product. Updates product title and summary.")
  public VariantResponseDto setDefaultVariant(@PathVariable String id) {
    VariantData response = grpcClient.setDefaultVariant(
        SetDefaultVariantRequest.newBuilder().setVariantId(id).build());
    return mapper.toResponseDto(response);
  }
}
