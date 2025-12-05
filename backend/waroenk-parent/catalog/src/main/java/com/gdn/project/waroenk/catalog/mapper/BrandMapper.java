package com.gdn.project.waroenk.catalog.mapper;

import com.gdn.project.waroenk.catalog.BrandData;
import com.gdn.project.waroenk.catalog.CreateBrandRequest;
import com.gdn.project.waroenk.catalog.MultipleBrandResponse;
import com.gdn.project.waroenk.catalog.UpdateBrandRequest;
import com.gdn.project.waroenk.catalog.dto.brand.BrandResponseDto;
import com.gdn.project.waroenk.catalog.dto.brand.CreateBrandRequestDto;
import com.gdn.project.waroenk.catalog.dto.brand.ListOfBrandResponseDto;
import com.gdn.project.waroenk.catalog.dto.brand.UpdateBrandRequestDto;
import com.gdn.project.waroenk.catalog.entity.Brand;
import com.google.protobuf.Timestamp;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.List;

@Mapper
public interface BrandMapper extends GenericMapper {

  BrandMapper INSTANCE = Mappers.getMapper(BrandMapper.class);

  BrandResponseDto toResponseDto(BrandData grpc);

  BrandResponseDto toResponseDto(Brand entity);

  default BrandData toResponseGrpc(Brand entity) {
    BrandData.Builder builder = BrandData.newBuilder();
    builder.setId(entity.getId());
    builder.setName(entity.getName());
    builder.setSlug(entity.getSlug());
    if (entity.getIconUrl() != null) {
      builder.setIconUrl(entity.getIconUrl());
    }
    if (entity.getCreatedAt() != null) {
      builder.setCreatedAt(toTimestamp(entity.getCreatedAt()));
    }
    if (entity.getUpdatedAt() != null) {
      builder.setUpdatedAt(toTimestamp(entity.getUpdatedAt()));
    }
    return builder.build();
  }

  default Brand toEntity(CreateBrandRequest request) {
    return Brand.builder()
        .name(request.getName())
        .slug(request.getSlug())
        .iconUrl(request.getIconUrl())
        .build();
  }

  default Brand toEntity(CreateBrandRequestDto dto) {
    return Brand.builder()
        .name(dto.name())
        .slug(dto.slug())
        .iconUrl(dto.iconUrl())
        .build();
  }

  default ListOfBrandResponseDto toResponseDto(MultipleBrandResponse grpc) {
    String token = grpc.getNextToken();
    Integer total = grpc.getTotal();
    List<BrandData> data = grpc.getDataList();
    return new ListOfBrandResponseDto(data.stream().map(this::toResponseDto).toList(), token, total);
  }

  default CreateBrandRequest toRequestGrpc(CreateBrandRequestDto dto) {
    CreateBrandRequest.Builder builder = CreateBrandRequest.newBuilder();
    builder.setName(dto.name());
    builder.setSlug(dto.slug());
    if (dto.iconUrl() != null) {
      builder.setIconUrl(dto.iconUrl());
    }
    return builder.build();
  }

  default UpdateBrandRequest toRequestGrpc(String id, UpdateBrandRequestDto dto) {
    UpdateBrandRequest.Builder builder = UpdateBrandRequest.newBuilder();
    builder.setId(id);
    if (dto.name() != null) builder.setName(dto.name());
    if (dto.slug() != null) builder.setSlug(dto.slug());
    return builder.build();
  }

  default Timestamp toTimestamp(Instant instant) {
    return Timestamp.newBuilder()
        .setSeconds(instant.getEpochSecond())
        .setNanos(instant.getNano())
        .build();
  }

  default Instant toInstant(Timestamp timestamp) {
    return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
  }
}



