package com.gdn.project.waroenk.catalog.mapper;

import com.gdn.project.waroenk.catalog.ContactInfo;
import com.gdn.project.waroenk.catalog.CreateMerchantRequest;
import com.gdn.project.waroenk.catalog.MerchantData;
import com.gdn.project.waroenk.catalog.MultipleMerchantResponse;
import com.gdn.project.waroenk.catalog.UpdateMerchantRequest;
import com.gdn.project.waroenk.catalog.dto.merchant.CreateMerchantRequestDto;
import com.gdn.project.waroenk.catalog.dto.merchant.ListOfMerchantResponseDto;
import com.gdn.project.waroenk.catalog.dto.merchant.MerchantResponseDto;
import com.gdn.project.waroenk.catalog.dto.merchant.UpdateMerchantRequestDto;
import com.gdn.project.waroenk.catalog.entity.Merchant;
import com.google.protobuf.Timestamp;
import org.apache.commons.lang3.ObjectUtils;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.List;

@Mapper
public interface MerchantMapper extends GenericMapper {

  MerchantMapper INSTANCE = Mappers.getMapper(MerchantMapper.class);

  default MerchantResponseDto toResponseDto(MerchantData grpc) {
    MerchantResponseDto.ContactInfoDto contactDto = null;
    if (ObjectUtils.isNotEmpty(grpc.getContact())) {
      contactDto = new MerchantResponseDto.ContactInfoDto(grpc.getContact().getPhone(), grpc.getContact().getEmail());
    }
    return new MerchantResponseDto(grpc.getId(),
        grpc.getName(),
        grpc.getCode(),
        grpc.getIconUrl(),
        grpc.getLocation(),
        contactDto,
        grpc.getRating(),
        toInstant(grpc.getCreatedAt()),
        toInstant(grpc.getUpdatedAt()));
  }

  default MerchantResponseDto toResponseDto(Merchant entity) {
    MerchantResponseDto.ContactInfoDto contactDto = null;
    if (entity.getContact() != null) {
      contactDto =
          new MerchantResponseDto.ContactInfoDto(entity.getContact().getPhone(), entity.getContact().getEmail());
    }
    return new MerchantResponseDto(entity.getId(),
        entity.getName(),
        entity.getCode(),
        entity.getIconUrl(),
        entity.getLocation(),
        contactDto,
        entity.getRating(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }

  default MerchantData toResponseGrpc(Merchant entity) {
    MerchantData.Builder builder = MerchantData.newBuilder();
    builder.setId(entity.getId());
    builder.setName(entity.getName());
    builder.setCode(entity.getCode());

    if (entity.getIconUrl() != null) {
      builder.setIconUrl(entity.getIconUrl());
    }

    if (entity.getLocation() != null) {
      builder.setLocation(entity.getLocation());
    }

    if (entity.getContact() != null) {
      ContactInfo.Builder contactBuilder = ContactInfo.newBuilder();
      if (entity.getContact().getPhone() != null) {
        contactBuilder.setPhone(entity.getContact().getPhone());
      }
      if (entity.getContact().getEmail() != null) {
        contactBuilder.setEmail(entity.getContact().getEmail());
      }
      builder.setContact(contactBuilder.build());
    }

    if (entity.getRating() != null) {
      builder.setRating(entity.getRating());
    }

    if (entity.getCreatedAt() != null) {
      builder.setCreatedAt(toTimestamp(entity.getCreatedAt()));
    }
    if (entity.getUpdatedAt() != null) {
      builder.setUpdatedAt(toTimestamp(entity.getUpdatedAt()));
    }

    return builder.build();
  }

  default Merchant toEntity(CreateMerchantRequest request) {
    Merchant.MerchantBuilder builder = Merchant.builder();
    builder.name(request.getName());
    builder.code(request.getCode());
    builder.iconUrl(request.getIconUrl());
    builder.location(request.getLocation());

    if (ObjectUtils.isNotEmpty(request.getContact())) {
      Merchant.ContactInfo contact = Merchant.ContactInfo.builder()
          .phone(request.getContact().getPhone())
          .email(request.getContact().getEmail())
          .build();
      builder.contact(contact);
    }

    builder.rating(request.getRating());

    return builder.build();
  }

  default Merchant toEntity(CreateMerchantRequestDto dto) {
    Merchant.MerchantBuilder builder = Merchant.builder();
    builder.name(dto.name());
    builder.code(dto.code());
    builder.iconUrl(dto.iconUrl());
    builder.location(dto.location());

    if (dto.contact() != null) {
      Merchant.ContactInfo contact =
          Merchant.ContactInfo.builder().phone(dto.contact().phone()).email(dto.contact().email()).build();
      builder.contact(contact);
    }

    if (dto.rating() != null) {
      builder.rating(dto.rating());
    }

    return builder.build();
  }

  default ListOfMerchantResponseDto toResponseDto(MultipleMerchantResponse grpc) {
    String token = grpc.getNextToken();
    Integer total = grpc.getTotal();
    List<MerchantData> data = grpc.getDataList();
    return new ListOfMerchantResponseDto(data.stream().map(this::toResponseDto).toList(), token, total);
  }

  default CreateMerchantRequest toRequestGrpc(CreateMerchantRequestDto dto) {
    CreateMerchantRequest.Builder builder = CreateMerchantRequest.newBuilder();
    builder.setName(dto.name());
    builder.setCode(dto.code());
    if (dto.iconUrl() != null) {
      builder.setIconUrl(dto.iconUrl());
    }
    if (dto.location() != null) {
      builder.setLocation(dto.location());
    }
    if (dto.contact() != null) {
      builder.setContact(ContactInfo.newBuilder()
          .setPhone(dto.contact().phone() != null ? dto.contact().phone() : "")
          .setEmail(dto.contact().email() != null ? dto.contact().email() : "")
          .build());
    }
    if (dto.rating() != null) {
      builder.setRating(dto.rating());
    }
    return builder.build();
  }

  default UpdateMerchantRequest toRequestGrpc(String id, UpdateMerchantRequestDto dto) {
    UpdateMerchantRequest.Builder builder = UpdateMerchantRequest.newBuilder();
    builder.setId(id);
    if (dto.name() != null)
      builder.setName(dto.name());
    if (dto.code() != null)
      builder.setCode(dto.code());
    if (dto.iconUrl() != null)
      builder.setIconUrl(dto.iconUrl());
    if (dto.location() != null)
      builder.setLocation(dto.location());
    if (dto.contact() != null) {
      builder.setContact(ContactInfo.newBuilder()
          .setPhone(dto.contact().phone() != null ? dto.contact().phone() : "")
          .setEmail(dto.contact().email() != null ? dto.contact().email() : "")
          .build());
    }
    if (dto.rating() != null)
      builder.setRating(dto.rating());
    return builder.build();
  }

  default Timestamp toTimestamp(Instant instant) {
    return Timestamp.newBuilder().setSeconds(instant.getEpochSecond()).setNanos(instant.getNano()).build();
  }

  default Instant toInstant(Timestamp timestamp) {
    return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
  }
}
