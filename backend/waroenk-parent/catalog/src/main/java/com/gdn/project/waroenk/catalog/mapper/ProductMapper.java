package com.gdn.project.waroenk.catalog.mapper;

import com.gdn.project.waroenk.catalog.CreateProductRequest;
import com.gdn.project.waroenk.catalog.MultipleProductResponse;
import com.gdn.project.waroenk.catalog.ProductData;
import com.gdn.project.waroenk.catalog.ProductSummary;
import com.gdn.project.waroenk.catalog.UpdateProductRequest;
import com.gdn.project.waroenk.catalog.dto.product.CreateProductRequestDto;
import com.gdn.project.waroenk.catalog.dto.product.ListOfProductResponseDto;
import com.gdn.project.waroenk.catalog.dto.product.ProductResponseDto;
import com.gdn.project.waroenk.catalog.dto.product.UpdateProductRequestDto;
import com.gdn.project.waroenk.catalog.entity.Product;
import com.google.protobuf.Timestamp;
import org.apache.commons.lang3.ObjectUtils;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.List;

@Mapper
public interface ProductMapper extends GenericMapper {

  ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

  ProductResponseDto toResponseDto(ProductData grpc);

  ProductResponseDto toResponseDto(Product entity);

  default ProductData toResponseGrpc(Product entity) {
    ProductData.Builder builder = ProductData.newBuilder();
    builder.setId(entity.getId());
    builder.setTitle(entity.getTitle());
    builder.setSku(entity.getSku());
    builder.setMerchantCode(entity.getMerchantCode());
    builder.setCategoryId(entity.getCategoryId());
    builder.setBrandId(entity.getBrandId());

    if (entity.getSummary() != null) {
      ProductSummary.Builder summaryBuilder = ProductSummary.newBuilder();
      if (entity.getSummary().getShortDescription() != null) {
        summaryBuilder.setShortDescription(entity.getSummary().getShortDescription());
      }
      if (entity.getSummary().getTags() != null) {
        summaryBuilder.addAllTags(entity.getSummary().getTags());
      }
      builder.setSummary(summaryBuilder.build());
    }

    if (entity.getDetailRef() != null) {
      builder.setDetailRef(entity.getDetailRef());
    }
    if (entity.getCreatedAt() != null) {
      builder.setCreatedAt(toTimestamp(entity.getCreatedAt()));
    }
    if (entity.getUpdatedAt() != null) {
      builder.setUpdatedAt(toTimestamp(entity.getUpdatedAt()));
    }

    return builder.build();
  }

  default Product toEntity(CreateProductRequest request) {
    Product.ProductBuilder builder = Product.builder();
    builder.title(request.getTitle());
    builder.sku(request.getSku());
    builder.merchantCode(request.getMerchantCode());
    builder.categoryId(request.getCategoryId());
    builder.brandId(request.getBrandId());

    if (ObjectUtils.isNotEmpty(request.getSummary())) {
      Product.ProductSummary summary = Product.ProductSummary.builder()
          .shortDescription(request.getSummary().getShortDescription())
          .tags(request.getSummary().getTagsList())
          .build();
      builder.summary(summary);
    }

    builder.detailRef(request.getDetailRef());

    return builder.build();
  }

  default Product toEntity(CreateProductRequestDto dto) {
    Product.ProductBuilder builder = Product.builder();
    builder.title(dto.title());
    builder.sku(dto.sku());
    builder.merchantCode(dto.merchantCode());
    builder.categoryId(dto.categoryId());
    builder.brandId(dto.brandId());

    if (dto.summary() != null) {
      Product.ProductSummary summary = Product.ProductSummary.builder()
          .shortDescription(dto.summary().shortDescription())
          .tags(dto.summary().tags())
          .build();
      builder.summary(summary);
    }

    if (dto.detailRef() != null) {
      builder.detailRef(dto.detailRef());
    }

    return builder.build();
  }

  default ListOfProductResponseDto toResponseDto(MultipleProductResponse grpc) {
    String token = grpc.getNextToken();
    Integer total = grpc.getTotal();
    List<ProductData> data = grpc.getDataList();
    return new ListOfProductResponseDto(data.stream().map(this::toResponseDto).toList(), token, total);
  }

  default CreateProductRequest toRequestGrpc(CreateProductRequestDto dto) {
    CreateProductRequest.Builder builder = CreateProductRequest.newBuilder();
    builder.setTitle(dto.title());
    builder.setSku(dto.sku());
    builder.setMerchantCode(dto.merchantCode());
    builder.setCategoryId(dto.categoryId());
    builder.setBrandId(dto.brandId());

    if (dto.summary() != null) {
      ProductSummary.Builder summaryBuilder = ProductSummary.newBuilder();
      if (dto.summary().shortDescription() != null) {
        summaryBuilder.setShortDescription(dto.summary().shortDescription());
      }
      if (dto.summary().tags() != null) {
        summaryBuilder.addAllTags(dto.summary().tags());
      }
      builder.setSummary(summaryBuilder.build());
    }

    if (dto.detailRef() != null) {
      builder.setDetailRef(dto.detailRef());
    }

    return builder.build();
  }

  default UpdateProductRequest toRequestGrpc(String id, UpdateProductRequestDto dto) {
    UpdateProductRequest.Builder builder = UpdateProductRequest.newBuilder();
    builder.setId(id);
    if (dto.title() != null) builder.setTitle(dto.title());
    if (dto.sku() != null) builder.setSku(dto.sku());
    if (dto.merchantCode() != null) builder.setMerchantCode(dto.merchantCode());
    if (dto.categoryId() != null) builder.setCategoryId(dto.categoryId());
    if (dto.brandId() != null) builder.setBrandId(dto.brandId());

    if (dto.summary() != null) {
      ProductSummary.Builder summaryBuilder = ProductSummary.newBuilder();
      if (dto.summary().shortDescription() != null) {
        summaryBuilder.setShortDescription(dto.summary().shortDescription());
      }
      if (dto.summary().tags() != null) {
        summaryBuilder.addAllTags(dto.summary().tags());
      }
      builder.setSummary(summaryBuilder.build());
    }

    if (dto.detailRef() != null) {
      builder.setDetailRef(dto.detailRef());
    }

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
