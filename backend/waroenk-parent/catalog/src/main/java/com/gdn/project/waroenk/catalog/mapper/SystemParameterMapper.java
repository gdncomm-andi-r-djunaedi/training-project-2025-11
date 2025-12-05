package com.gdn.project.waroenk.catalog.mapper;

import com.gdn.project.waroenk.catalog.MultipleSystemParameterResponse;
import com.gdn.project.waroenk.catalog.SystemParameterData;
import com.gdn.project.waroenk.catalog.UpsertSystemParameterRequest;
import com.gdn.project.waroenk.catalog.dto.systemparameter.ListOfSystemParameterResponseDto;
import com.gdn.project.waroenk.catalog.dto.systemparameter.SystemParameterResponseDto;
import com.gdn.project.waroenk.catalog.dto.systemparameter.UpsertSystemParameterRequestDto;
import com.gdn.project.waroenk.catalog.entity.SystemParameter;
import com.google.protobuf.Timestamp;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.List;

@Mapper
public interface SystemParameterMapper extends GenericMapper {

  SystemParameterMapper INSTANCE = Mappers.getMapper(SystemParameterMapper.class);

  SystemParameterResponseDto toResponseDto(SystemParameterData grpc);

  SystemParameterResponseDto toResponseDto(SystemParameter entity);

  default SystemParameterData toResponseGrpc(SystemParameter entity) {
    SystemParameterData.Builder builder = SystemParameterData.newBuilder();
    builder.setId(entity.getId());
    builder.setVariable(entity.getVariable());
    if (entity.getData() != null) {
      builder.setData(entity.getData());
    }
    if (entity.getDescription() != null) {
      builder.setDescription(entity.getDescription());
    }
    if (entity.getCreatedAt() != null) {
      builder.setCreatedAt(toTimestamp(entity.getCreatedAt()));
    }
    if (entity.getUpdatedAt() != null) {
      builder.setUpdatedAt(toTimestamp(entity.getUpdatedAt()));
    }
    return builder.build();
  }

  default SystemParameter toEntity(UpsertSystemParameterRequest request) {
    return SystemParameter.builder()
        .variable(request.getVariable())
        .data(request.getData())
        .description(request.getDescription())
        .build();
  }

  default SystemParameter toEntity(UpsertSystemParameterRequestDto dto) {
    return SystemParameter.builder()
        .variable(dto.variable())
        .data(dto.data())
        .description(dto.description())
        .build();
  }

  default ListOfSystemParameterResponseDto toResponseDto(MultipleSystemParameterResponse grpc) {
    String token = grpc.getNextToken();
    Integer total = grpc.getTotal();
    List<SystemParameterData> data = grpc.getDataList();
    return new ListOfSystemParameterResponseDto(data.stream().map(this::toResponseDto).toList(), token, total);
  }

  default UpsertSystemParameterRequest toRequestGrpc(UpsertSystemParameterRequestDto dto) {
    UpsertSystemParameterRequest.Builder builder = UpsertSystemParameterRequest.newBuilder();
    builder.setVariable(dto.variable());
    builder.setData(dto.data());
    if (dto.description() != null) {
      builder.setDescription(dto.description());
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






