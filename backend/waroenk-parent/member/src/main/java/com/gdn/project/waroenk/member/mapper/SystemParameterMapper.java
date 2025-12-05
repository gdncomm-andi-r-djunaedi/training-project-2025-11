package com.gdn.project.waroenk.member.mapper;

import com.gdn.project.waroenk.member.MultipleSystemParameterRequest;
import com.gdn.project.waroenk.member.MultipleSystemParameterResponse;
import com.gdn.project.waroenk.member.SystemParameterData;
import com.gdn.project.waroenk.member.UpsertSystemParameterRequest;
import com.gdn.project.waroenk.member.dto.ListOfSystemParameterResponseDto;
import com.gdn.project.waroenk.member.dto.MultipleSystemParameterRequestDto;
import com.gdn.project.waroenk.member.dto.SystemParameterResponseDto;
import com.gdn.project.waroenk.member.dto.UpsertSystemParameterRequestDto;
import com.gdn.project.waroenk.member.entity.SystemParameter;
import com.google.protobuf.Timestamp;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Mapper
public interface SystemParameterMapper extends GenericMapper {

  SystemParameterMapper INSTANCE = Mappers.getMapper(SystemParameterMapper.class);

  SystemParameterResponseDto toResponseDto(SystemParameterData grpc);

  SystemParameterResponseDto toResponseDto(SystemParameter entity);

  default SystemParameterData toResponseGrpc(SystemParameter entity) {
    SystemParameterData.Builder builder = SystemParameterData.newBuilder();
    builder.setId(entity.getId().toString());
    builder.setData(entity.getData());
    builder.setVariable(entity.getVariable());
    builder.setDescription(entity.getDescription());

    // Convert LocalDateTime to epoch seconds using system timezone
    Instant createdInstant = entity.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant();
    builder.setCreatedAt(Timestamp.newBuilder()
        .setSeconds(createdInstant.getEpochSecond())
        .setNanos(createdInstant.getNano())
        .build());

    Instant updatedInstant = entity.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant();
    builder.setUpdatedAt(Timestamp.newBuilder()
        .setSeconds(updatedInstant.getEpochSecond())
        .setNanos(updatedInstant.getNano())
        .build());

    return builder.build();
  }

  default SystemParameterData toResponseGrpc(SystemParameterResponseDto responseDto) {
    SystemParameterData.Builder builder = SystemParameterData.newBuilder();
    builder.setData(responseDto.data());
    builder.setVariable(responseDto.variable());
    builder.setDescription(responseDto.description());
    Instant createdAt = responseDto.createdAt().atZone(ZoneId.systemDefault()).toInstant();
    Instant updatedAt = responseDto.updatedAt().atZone(ZoneId.systemDefault()).toInstant();
    builder.setCreatedAt(Timestamp.newBuilder()
        .setSeconds(createdAt.getEpochSecond())
        .setNanos(createdAt.getNano())
        .build());
    builder.setUpdatedAt(Timestamp.newBuilder()
        .setSeconds(updatedAt.getEpochSecond())
        .setNanos(updatedAt.getNano())
        .build());

    return builder.build();
  }

  SystemParameter toSystemParameterEntity(SystemParameterData grpc);

  SystemParameter toSystemParameterEntity(UpsertSystemParameterRequest grpc);

  default ListOfSystemParameterResponseDto toResponseDto(MultipleSystemParameterResponse grpc) {
    String token = grpc.getNextToken();
    Integer total = grpc.getTotal();
    List<SystemParameterData> data = grpc.getDataList();
    return new ListOfSystemParameterResponseDto(data.stream().map(this::toResponseDto).toList(), token, total);
  }

  default MultipleSystemParameterRequest toRequestGrpc(MultipleSystemParameterRequestDto requestDto) {
    MultipleSystemParameterRequest.Builder builder = MultipleSystemParameterRequest.newBuilder();
    builder.setSize(requestDto.size());
    // Only set optional fields if they have values (protobuf doesn't allow null)
    if (requestDto.variable() != null) {
      builder.setVariable(requestDto.variable());
    }
    if (requestDto.cursor() != null) {
      builder.setCursor(requestDto.cursor());
    }
    return builder.build();
  }

  default UpsertSystemParameterRequest toRequestGrpc(UpsertSystemParameterRequestDto requestDto) {
    UpsertSystemParameterRequest.Builder builder = UpsertSystemParameterRequest.newBuilder();
    // Only set fields if they have values (protobuf doesn't allow null)
    if (requestDto.variable() != null) {
      builder.setVariable(requestDto.variable());
    }
    if (requestDto.data() != null) {
      builder.setData(requestDto.data());
    }
    if (requestDto.description() != null) {
      builder.setDescription(requestDto.description());
    }
    return builder.build();
  }

  default Instant toInstant(Timestamp value) {
    return Instant.ofEpochSecond(value.getSeconds(), value.getNanos());
  }

  default LocalDateTime toLocalDateTime(Timestamp value) {
    // Convert protobuf Timestamp to LocalDateTime using system timezone
    return LocalDateTime.ofInstant(Instant.ofEpochSecond(value.getSeconds(), value.getNanos()), ZoneId.systemDefault());
  }
}
