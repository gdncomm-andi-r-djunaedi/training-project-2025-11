package com.gdn.project.waroenk.catalog.controller.http;

import com.gdn.project.waroenk.catalog.FilterMerchantRequest;
import com.gdn.project.waroenk.catalog.FindMerchantByCodeRequest;
import com.gdn.project.waroenk.catalog.MerchantData;
import com.gdn.project.waroenk.catalog.MerchantServiceGrpc;
import com.gdn.project.waroenk.catalog.MultipleMerchantResponse;
import com.gdn.project.waroenk.catalog.constant.ApiConstant;
import com.gdn.project.waroenk.catalog.dto.BasicDto;
import com.gdn.project.waroenk.catalog.dto.merchant.CreateMerchantRequestDto;
import com.gdn.project.waroenk.catalog.dto.merchant.ListOfMerchantResponseDto;
import com.gdn.project.waroenk.catalog.dto.merchant.MerchantResponseDto;
import com.gdn.project.waroenk.catalog.dto.merchant.UpdateMerchantRequestDto;
import com.gdn.project.waroenk.catalog.mapper.MerchantMapper;
import com.gdn.project.waroenk.common.Basic;
import com.gdn.project.waroenk.common.Id;
import com.gdn.project.waroenk.common.SortBy;
import jakarta.validation.Valid;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping(ApiConstant.BASE_PATH)
@RestController("catalogHttpMerchantController")
public class MerchantController {
  private static final MerchantMapper mapper = MerchantMapper.INSTANCE;
  private final MerchantServiceGrpc.MerchantServiceBlockingStub grpcClient;

  @Autowired
  public MerchantController(@GrpcClient("catalog-service") MerchantServiceGrpc.MerchantServiceBlockingStub grpcClient) {
    this.grpcClient = grpcClient;
  }

  @PostMapping("/merchant")
  public MerchantResponseDto createMerchant(@Valid @RequestBody CreateMerchantRequestDto requestDto) {
    MerchantData response = grpcClient.createMerchant(mapper.toRequestGrpc(requestDto));
    return mapper.toResponseDto(response);
  }

  @PutMapping("/merchant/{id}")
  public MerchantResponseDto updateMerchant(@PathVariable String id, @RequestBody UpdateMerchantRequestDto requestDto) {
    MerchantData response = grpcClient.updateMerchant(mapper.toRequestGrpc(id, requestDto));
    return mapper.toResponseDto(response);
  }

  @DeleteMapping("/merchant/{id}")
  public BasicDto deleteMerchant(@PathVariable String id) {
    Basic response = grpcClient.deleteMerchant(Id.newBuilder().setValue(id).build());
    return mapper.toBasicDto(response);
  }

  @GetMapping("/merchant/{id}")
  public MerchantResponseDto findMerchantById(@PathVariable String id) {
    MerchantData response = grpcClient.findMerchantById(Id.newBuilder().setValue(id).build());
    return mapper.toResponseDto(response);
  }

  @GetMapping("/merchant/by-code")
  public MerchantResponseDto findMerchantByCode(@RequestParam String code) {
    MerchantData response = grpcClient.findMerchantByCode(FindMerchantByCodeRequest.newBuilder().setCode(code).build());
    return mapper.toResponseDto(response);
  }

  @GetMapping("/merchant/filter")
  public ListOfMerchantResponseDto filterMerchants(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String code,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false, defaultValue = "id") String sortBy,
      @RequestParam(required = false, defaultValue = "asc") String sortDirection) {
    FilterMerchantRequest.Builder builder = FilterMerchantRequest.newBuilder().setSize(size);
    if (StringUtils.isNotBlank(name)) builder.setName(name);
    if (StringUtils.isNotBlank(code)) builder.setCode(code);
    if (StringUtils.isNotBlank(cursor)) builder.setCursor(cursor);
    builder.setSortBy(SortBy.newBuilder().setField(sortBy).setDirection(sortDirection).build());
    
    MultipleMerchantResponse response = grpcClient.filterMerchant(builder.build());
    return mapper.toResponseDto(response);
  }
}






