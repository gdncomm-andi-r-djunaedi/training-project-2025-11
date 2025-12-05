package com.gdn.project.waroenk.catalog.mapper;

import com.gdn.project.waroenk.catalog.CategoryData;
import com.gdn.project.waroenk.catalog.CategoryNode;
import com.gdn.project.waroenk.catalog.CategoryTreeResponse;
import com.gdn.project.waroenk.catalog.CreateCategoryRequest;
import com.gdn.project.waroenk.catalog.MultipleCategoryResponse;
import com.gdn.project.waroenk.catalog.UpdateCategoryRequest;
import com.gdn.project.waroenk.catalog.dto.category.CategoryResponseDto;
import com.gdn.project.waroenk.catalog.dto.category.CategoryTreeNodeDto;
import com.gdn.project.waroenk.catalog.dto.category.CreateCategoryRequestDto;
import com.gdn.project.waroenk.catalog.dto.category.ListOfCategoryResponseDto;
import com.gdn.project.waroenk.catalog.dto.category.UpdateCategoryRequestDto;
import com.gdn.project.waroenk.catalog.entity.Category;
import com.google.protobuf.Timestamp;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.List;

@Mapper
public interface CategoryMapper extends GenericMapper {

  CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);

  CategoryResponseDto toResponseDto(CategoryData grpc);

  CategoryResponseDto toResponseDto(Category entity);

  default CategoryData toResponseGrpc(Category entity) {
    CategoryData.Builder builder = CategoryData.newBuilder();
    builder.setId(entity.getId());
    builder.setName(entity.getName());
    builder.setSlug(entity.getSlug());
    if (entity.getIconUrl() != null) {
      builder.setIconUrl(entity.getIconUrl());
    }
    if (entity.getParentId() != null) {
      builder.setParentId(entity.getParentId());
    }
    if (entity.getCreatedAt() != null) {
      builder.setCreatedAt(toTimestamp(entity.getCreatedAt()));
    }
    if (entity.getUpdatedAt() != null) {
      builder.setUpdatedAt(toTimestamp(entity.getUpdatedAt()));
    }
    return builder.build();
  }

  default Category toEntity(CreateCategoryRequest request) {
    return Category.builder()
        .name(request.getName())
        .slug(request.getSlug())
        .iconUrl(request.getIconUrl())
        .parentId(request.getParentId())
        .build();
  }

  default Category toEntity(CreateCategoryRequestDto dto) {
    return Category.builder()
        .name(dto.name())
        .slug(dto.slug())
        .iconUrl(dto.iconUrl())
        .parentId(dto.parentId())
        .build();
  }

  default ListOfCategoryResponseDto toResponseDto(MultipleCategoryResponse grpc) {
    String token = grpc.getNextToken();
    Integer total = grpc.getTotal();
    List<CategoryData> data = grpc.getDataList();
    return new ListOfCategoryResponseDto(data.stream().map(this::toResponseDto).toList(), token, total);
  }

  default CategoryTreeNodeDto toTreeNodeDto(CategoryNode node) {
    return new CategoryTreeNodeDto(
        node.getId(),
        node.getIconUrl(),
        node.getSlug(),
        node.getName(),
        node.getChildrenList().stream().map(this::toTreeNodeDto).toList()
    );
  }

  default List<CategoryTreeNodeDto> toTreeNodeDtoList(CategoryTreeResponse response) {
    return response.getNodesList().stream().map(this::toTreeNodeDto).toList();
  }

  default CreateCategoryRequest toRequestGrpc(CreateCategoryRequestDto dto) {
    CreateCategoryRequest.Builder builder = CreateCategoryRequest.newBuilder();
    builder.setName(dto.name());
    builder.setSlug(dto.slug());
    if (dto.iconUrl() != null) {
      builder.setIconUrl(dto.iconUrl());
    }
    if (dto.parentId() != null) {
      builder.setParentId(dto.parentId());
    }
    return builder.build();
  }

  default UpdateCategoryRequest toRequestGrpc(String id, UpdateCategoryRequestDto dto) {
    UpdateCategoryRequest.Builder builder = UpdateCategoryRequest.newBuilder();
    builder.setId(id);
    if (dto.name() != null) builder.setName(dto.name());
    if (dto.slug() != null) builder.setSlug(dto.slug());
    if (dto.parentId() != null) builder.setParentId(dto.parentId());
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



