package com.gdn.project.waroenk.catalog.controller.http;

import com.gdn.project.waroenk.catalog.BrandData;
import com.gdn.project.waroenk.catalog.BrandServiceGrpc;
import com.gdn.project.waroenk.catalog.FilterBrandRequest;
import com.gdn.project.waroenk.catalog.FindBrandBySlugRequest;
import com.gdn.project.waroenk.catalog.MultipleBrandResponse;
import com.gdn.project.waroenk.catalog.constant.ApiConstant;
import com.gdn.project.waroenk.catalog.dto.BasicDto;
import com.gdn.project.waroenk.catalog.dto.brand.BrandResponseDto;
import com.gdn.project.waroenk.catalog.dto.brand.CreateBrandRequestDto;
import com.gdn.project.waroenk.catalog.dto.brand.ListOfBrandResponseDto;
import com.gdn.project.waroenk.catalog.dto.brand.UpdateBrandRequestDto;
import com.gdn.project.waroenk.catalog.mapper.BrandMapper;
import com.gdn.project.waroenk.common.Basic;
import com.gdn.project.waroenk.common.Id;
import com.gdn.project.waroenk.common.SortBy;
import jakarta.validation.Valid;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping(ApiConstant.BASE_PATH)
@RestController("catalogHttpBrandController")
public class BrandController {
  private static final BrandMapper mapper = BrandMapper.INSTANCE;
  private final BrandServiceGrpc.BrandServiceBlockingStub grpcClient;

  @Autowired
  public BrandController(@GrpcClient("catalog-service") BrandServiceGrpc.BrandServiceBlockingStub grpcClient) {
    this.grpcClient = grpcClient;
  }

  @PostMapping("/brand")
  public BrandResponseDto createBrand(@Valid @RequestBody CreateBrandRequestDto requestDto) {
    BrandData response = grpcClient.createBrand(mapper.toRequestGrpc(requestDto));
    return mapper.toResponseDto(response);
  }

  @PutMapping("/brand/{id}")
  public BrandResponseDto updateBrand(@PathVariable String id, @RequestBody UpdateBrandRequestDto requestDto) {
    BrandData response = grpcClient.updateBrand(mapper.toRequestGrpc(id, requestDto));
    return mapper.toResponseDto(response);
  }

  @DeleteMapping("/brand/{id}")
  public BasicDto deleteBrand(@PathVariable String id) {
    Basic response = grpcClient.deleteBrand(Id.newBuilder().setValue(id).build());
    return mapper.toBasicDto(response);
  }

  @GetMapping("/brand/{id}")
  public BrandResponseDto findBrandById(@PathVariable String id) {
    BrandData response = grpcClient.findBrandById(Id.newBuilder().setValue(id).build());
    return mapper.toResponseDto(response);
  }

  @GetMapping("/brand/by-slug")
  public BrandResponseDto findBrandBySlug(@RequestParam String slug) {
    BrandData response = grpcClient.findBrandBySlug(FindBrandBySlugRequest.newBuilder().setSlug(slug).build());
    return mapper.toResponseDto(response);
  }

  @GetMapping("/brand/filter")
  public ListOfBrandResponseDto filterBrands(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String slug,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false, defaultValue = "id") String sortBy,
      @RequestParam(required = false, defaultValue = "asc") String sortDirection) {
    FilterBrandRequest.Builder builder = FilterBrandRequest.newBuilder().setSize(size);
    if (StringUtils.isNotBlank(name)) builder.setName(name);
    if (StringUtils.isNotBlank(slug)) builder.setSlug(slug);
    if (StringUtils.isNotBlank(cursor)) builder.setCursor(cursor);
    builder.setSortBy(SortBy.newBuilder().setField(sortBy).setDirection(sortDirection).build());
    
    MultipleBrandResponse response = grpcClient.filterBrand(builder.build());
    return mapper.toResponseDto(response);
  }
}






