package com.gdn.project.waroenk.catalog.mapper;

import com.gdn.project.waroenk.catalog.CreateVariantRequest;
import com.gdn.project.waroenk.catalog.MultipleVariantResponse;
import com.gdn.project.waroenk.catalog.UpdateVariantRequest;
import com.gdn.project.waroenk.catalog.VariantData;
import com.gdn.project.waroenk.catalog.VariantMedia;
import com.gdn.project.waroenk.catalog.dto.variant.CreateVariantRequestDto;
import com.gdn.project.waroenk.catalog.dto.variant.ListOfVariantResponseDto;
import com.gdn.project.waroenk.catalog.dto.variant.UpdateVariantRequestDto;
import com.gdn.project.waroenk.catalog.dto.variant.VariantResponseDto;
import com.gdn.project.waroenk.catalog.entity.Variant;
import com.google.protobuf.Struct;
import com.google.protobuf.Timestamp;
import com.google.protobuf.Value;
import org.apache.commons.lang3.ObjectUtils;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface VariantMapper extends GenericMapper {

  VariantMapper INSTANCE = Mappers.getMapper(VariantMapper.class);

  default VariantResponseDto toResponseDto(VariantData grpc) {
    List<VariantResponseDto.VariantMediaDto> mediaDtos = new ArrayList<>();
    if (ObjectUtils.isNotEmpty(grpc.getMediaList())) {
      for (VariantMedia media : grpc.getMediaList()) {
        mediaDtos.add(new VariantResponseDto.VariantMediaDto(media.getUrl(),
            media.getType(),
            media.getSortOrder(),
            media.getAltText()));
      }
    }

    return new VariantResponseDto(grpc.getId(),
        grpc.getSku(),
        grpc.getSubSku(),
        grpc.getTitle(),
        grpc.getPrice(),
        grpc.getIsDefault(),
        structToMap(grpc.getAttributes()),
        grpc.getThumbnail(),
        mediaDtos,
        toInstant(grpc.getCreatedAt()),
        toInstant(grpc.getUpdatedAt()));
  }

  default VariantResponseDto toResponseDto(Variant entity) {
    List<VariantResponseDto.VariantMediaDto> mediaDtos = new ArrayList<>();
    if (entity.getMedia() != null) {
      for (Variant.VariantMedia media : entity.getMedia()) {
        mediaDtos.add(new VariantResponseDto.VariantMediaDto(media.getUrl(),
            media.getType(),
            media.getSortOrder(),
            media.getAltText()));
      }
    }

    return new VariantResponseDto(entity.getId(),
        entity.getSku(),
        entity.getSubSku(),
        entity.getTitle(),
        entity.getPrice(),
        entity.getIsDefault(),
        entity.getAttributes(),
        entity.getThumbnail(),
        mediaDtos,
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }

  default VariantData toResponseGrpc(Variant entity) {
    VariantData.Builder builder = VariantData.newBuilder();
    builder.setId(entity.getId());
    builder.setSku(entity.getSku());
    builder.setSubSku(entity.getSubSku());

    if (entity.getTitle() != null) {
      builder.setTitle(entity.getTitle());
    }

    builder.setPrice(entity.getPrice());
    builder.setIsDefault(entity.getIsDefault() != null && entity.getIsDefault());

    if (entity.getAttributes() != null) {
      builder.setAttributes(mapToStruct(entity.getAttributes()));
    }

    if (entity.getThumbnail() != null) {
      builder.setThumbnail(entity.getThumbnail());
    }

    if (entity.getMedia() != null) {
      for (Variant.VariantMedia media : entity.getMedia()) {
        VariantMedia.Builder mediaBuilder = VariantMedia.newBuilder()
            .setUrl(media.getUrl())
            .setType(media.getType())
            .setSortOrder(media.getSortOrder() != null ? media.getSortOrder() : 0);
        if (media.getAltText() != null) {
          mediaBuilder.setAltText(media.getAltText());
        }
        builder.addMedia(mediaBuilder.build());
      }
    }

    if (entity.getCreatedAt() != null) {
      builder.setCreatedAt(toTimestamp(entity.getCreatedAt()));
    }
    if (entity.getUpdatedAt() != null) {
      builder.setUpdatedAt(toTimestamp(entity.getUpdatedAt()));
    }

    return builder.build();
  }

  default Variant toEntity(CreateVariantRequest request) {
    List<Variant.VariantMedia> mediaList = new ArrayList<>();
    if (request.getMediaList() != null) {
      for (VariantMedia media : request.getMediaList()) {
        mediaList.add(Variant.VariantMedia.builder()
            .url(media.getUrl())
            .type(media.getType())
            .sortOrder(media.getSortOrder())
            .altText(media.getAltText())
            .build());
      }
    }

    return Variant.builder()
        .sku(request.getSku())
        .title(request.getTitle())
        .price(request.getPrice())
        .isDefault(request.getIsDefault())
        .attributes(structToMap(request.getAttributes()))
        .thumbnail(request.getThumbnail())
        .media(mediaList)
        .build();
  }

  default Variant toEntity(CreateVariantRequestDto dto) {
    List<Variant.VariantMedia> mediaList = new ArrayList<>();
    if (dto.media() != null) {
      for (CreateVariantRequestDto.VariantMediaDto media : dto.media()) {
        mediaList.add(Variant.VariantMedia.builder()
            .url(media.url())
            .type(media.type())
            .sortOrder(media.sortOrder())
            .altText(media.altText())
            .build());
      }
    }

    return Variant.builder()
        .sku(dto.sku())
        .title(dto.title())
        .price(dto.price())
        .isDefault(dto.isDefault() != null && dto.isDefault())
        .attributes(dto.attributes())
        .thumbnail(dto.thumbnail())
        .media(mediaList)
        .build();
  }

  default ListOfVariantResponseDto toResponseDto(MultipleVariantResponse grpc) {
    String token = grpc.getNextToken();
    Integer total = grpc.getTotal();
    List<VariantData> data = grpc.getDataList();
    return new ListOfVariantResponseDto(data.stream().map(this::toResponseDto).toList(), token, total);
  }

  default CreateVariantRequest toRequestGrpc(CreateVariantRequestDto dto) {
    CreateVariantRequest.Builder builder = CreateVariantRequest.newBuilder();
    builder.setSku(dto.sku());
    builder.setTitle(dto.title());
    builder.setPrice(dto.price());
    builder.setIsDefault(dto.isDefault() != null && dto.isDefault());
    if (dto.attributes() != null) {
      builder.setAttributes(mapToStruct(dto.attributes()));
    }
    if (dto.thumbnail() != null) {
      builder.setThumbnail(dto.thumbnail());
    }
    if (dto.media() != null) {
      for (CreateVariantRequestDto.VariantMediaDto media : dto.media()) {
        VariantMedia.Builder mediaBuilder = VariantMedia.newBuilder()
            .setUrl(media.url())
            .setType(media.type())
            .setSortOrder(media.sortOrder() != null ? media.sortOrder() : 0);
        if (media.altText() != null) {
          mediaBuilder.setAltText(media.altText());
        }
        builder.addMedia(mediaBuilder.build());
      }
    }
    return builder.build();
  }

  default UpdateVariantRequest toRequestGrpc(String id, UpdateVariantRequestDto dto) {
    UpdateVariantRequest.Builder builder = UpdateVariantRequest.newBuilder();
    builder.setId(id);
    if (dto.sku() != null)
      builder.setSku(dto.sku());
    if (dto.title() != null)
      builder.setTitle(dto.title());
    if (dto.price() != null)
      builder.setPrice(dto.price());
    if (dto.isDefault() != null)
      builder.setIsDefault(dto.isDefault());
    if (dto.attributes() != null) {
      builder.setAttributes(mapToStruct(dto.attributes()));
    }
    if (dto.thumbnail() != null) {
      builder.setThumbnail(dto.thumbnail());
    }
    if (dto.media() != null) {
      for (UpdateVariantRequestDto.VariantMediaDto media : dto.media()) {
        VariantMedia.Builder mediaBuilder = VariantMedia.newBuilder()
            .setUrl(media.url())
            .setType(media.type())
            .setSortOrder(media.sortOrder() != null ? media.sortOrder() : 0);
        if (media.altText() != null) {
          mediaBuilder.setAltText(media.altText());
        }
        builder.addMedia(mediaBuilder.build());
      }
    }
    return builder.build();
  }

  default Struct mapToStruct(Map<String, Object> map) {
    Struct.Builder structBuilder = Struct.newBuilder();
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      structBuilder.putFields(entry.getKey(), objectToValue(entry.getValue()));
    }
    return structBuilder.build();
  }

  default Value objectToValue(Object obj) {
    if (obj == null) {
      return Value.newBuilder().setNullValue(com.google.protobuf.NullValue.NULL_VALUE).build();
    } else if (obj instanceof String string) {
      return Value.newBuilder().setStringValue(string).build();
    } else if (obj instanceof Number number) {
      return Value.newBuilder().setNumberValue(number.doubleValue()).build();
    } else if (obj instanceof Boolean bool) {
      return Value.newBuilder().setBoolValue(bool).build();
    } else {
      return Value.newBuilder().setStringValue(obj.toString()).build();
    }
  }

  default Map<String, Object> structToMap(Struct struct) {
    Map<String, Object> map = new HashMap<>();
    if (ObjectUtils.isEmpty(struct)) {
      return map;
    }
    for (Map.Entry<String, Value> entry : struct.getFieldsMap().entrySet()) {
      map.put(entry.getKey(), valueToObject(entry.getValue()));
    }
    return map;
  }

  default Object valueToObject(Value value) {
    return switch (value.getKindCase()) {
      case STRING_VALUE -> value.getStringValue();
      case NUMBER_VALUE -> value.getNumberValue();
      case BOOL_VALUE -> value.getBoolValue();
      case NULL_VALUE -> null;
      default -> value.getStringValue();
    };
  }

  default Timestamp toTimestamp(Instant instant) {
    return Timestamp.newBuilder().setSeconds(instant.getEpochSecond()).setNanos(instant.getNano()).build();
  }

  default Instant toInstant(Timestamp timestamp) {
    return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
  }
}
