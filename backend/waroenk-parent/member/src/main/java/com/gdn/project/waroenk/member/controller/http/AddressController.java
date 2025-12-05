package com.gdn.project.waroenk.member.controller.http;

import com.gdn.project.waroenk.common.Basic;
import com.gdn.project.waroenk.common.Id;
import com.gdn.project.waroenk.common.SortBy;
import com.gdn.project.waroenk.member.AddressData;
import com.gdn.project.waroenk.member.AddressServiceGrpc;
import com.gdn.project.waroenk.member.FilterAddressRequest;
import com.gdn.project.waroenk.member.FindUserAddressRequest;
import com.gdn.project.waroenk.member.MultipleAddressResponse;
import com.gdn.project.waroenk.member.UpsertAddressRequest;
import com.gdn.project.waroenk.member.constant.ApiConstant;
import com.gdn.project.waroenk.member.constant.Sort;
import com.gdn.project.waroenk.member.dto.AddressResponseDto;
import com.gdn.project.waroenk.member.dto.BasicDto;
import com.gdn.project.waroenk.member.dto.ListOfAddressResponseDto;
import com.gdn.project.waroenk.member.dto.SetDefaultAddressRequestDto;
import com.gdn.project.waroenk.member.dto.UpsertAddressRequestDto;
import com.gdn.project.waroenk.member.mapper.AddressMapper;
import jakarta.validation.Valid;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(ApiConstant.BASE_PATH)
@RestController("memberHttpAddressController")
public class AddressController {

  private static final AddressMapper mapper = AddressMapper.INSTANCE;
  private final AddressServiceGrpc.AddressServiceBlockingStub grpcClient;

  @Autowired
  public AddressController(@GrpcClient("member-service") AddressServiceGrpc.AddressServiceBlockingStub grpcClient) {
    this.grpcClient = grpcClient;
  }

  @PostMapping("/address")
  public AddressResponseDto upsertAddress(@Valid @RequestBody UpsertAddressRequestDto requestDto) {
    UpsertAddressRequest request = mapper.toUpsertAddressRequestGrpc(requestDto);
    AddressData response = grpcClient.upsertAddress(request);
    return mapper.toAddressResponseDto(response);
  }

  @DeleteMapping("/address")
  public BasicDto deleteAddress(@RequestParam String id) {
    Id request = Id.newBuilder().setValue(id).build();
    Basic response = grpcClient.deleteAddressById(request);
    return mapper.toBasicDto(response);
  }

  @PutMapping("/address/default")
  public BasicDto setDefaultAddress(@Valid @RequestBody SetDefaultAddressRequestDto requestDto) {
    FindUserAddressRequest request = FindUserAddressRequest.newBuilder()
        .setUserId(requestDto.userId())
        .setLabel(requestDto.label())
        .build();
    Basic response = grpcClient.setUserDefaultAddress(request);
    return mapper.toBasicDto(response);
  }

  @GetMapping("/address")
  public AddressResponseDto findAddressById(@RequestParam String id) {
    Id request = Id.newBuilder().setValue(id).build();
    AddressData response = grpcClient.findAddressById(request);
    return mapper.toAddressResponseDto(response);
  }

  @GetMapping("/address/find-one")
  public AddressResponseDto findAddressByLabel(
      @RequestParam String userId,
      @RequestParam String label) {
    FindUserAddressRequest request = FindUserAddressRequest.newBuilder()
        .setUserId(userId)
        .setLabel(label)
        .build();
    AddressData response = grpcClient.findOneUserAddressByLabel(request);
    return mapper.toAddressResponseDto(response);
  }

  @GetMapping("/address/filter")
  public ListOfAddressResponseDto filterAddresses(
      @RequestParam String userId,
      @RequestParam(required = false) String label,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false, defaultValue = "id") String sortBy,
      @RequestParam(required = false, defaultValue = "asc") Sort sortDirection) {

    FilterAddressRequest.Builder requestBuilder = FilterAddressRequest.newBuilder();
    requestBuilder.setUser(userId);
    requestBuilder.setSize(size);

    if (StringUtils.isNotBlank(label)) {
      requestBuilder.setLabel(label);
    }
    if (StringUtils.isNotBlank(cursor)) {
      requestBuilder.setCursor(cursor);
    }
    if (StringUtils.isNotBlank(sortBy)) {
      SortBy.Builder sortBuilder = SortBy.newBuilder();
      sortBuilder.setField(sortBy);
      if (ObjectUtils.isNotEmpty(sortDirection)) {
        sortBuilder.setDirection(sortDirection.getShortName());
      }
      requestBuilder.setSortBy(sortBuilder.build());
    }

    MultipleAddressResponse response = grpcClient.filterUserAddress(requestBuilder.build());
    return mapper.toListOfAddressResponseDto(response);
  }
}







