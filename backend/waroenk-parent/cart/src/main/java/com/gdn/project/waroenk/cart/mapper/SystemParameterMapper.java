package com.gdn.project.waroenk.cart.mapper;

import com.gdn.project.waroenk.cart.*;
import com.gdn.project.waroenk.cart.dto.systemparameter.*;
import com.gdn.project.waroenk.cart.entity.SystemParameter;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface SystemParameterMapper extends GenericMapper {

    SystemParameterMapper INSTANCE = Mappers.getMapper(SystemParameterMapper.class);

    // Entity to DTO
    default SystemParameterResponseDto toResponseDto(SystemParameter entity) {
        if (entity == null) {
            return null;
        }
        return new SystemParameterResponseDto(
                entity.getId(),
                entity.getVariable(),
                entity.getData(),
                entity.getDescription(),
                entity.getType(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    // Entity to gRPC
    default SystemParameterData toResponseGrpc(SystemParameter entity) {
        if (entity == null) {
            return null;
        }
        SystemParameterData.Builder builder = SystemParameterData.newBuilder();
        if (entity.getId() != null) builder.setId(entity.getId());
        if (entity.getVariable() != null) builder.setVariable(entity.getVariable());
        if (entity.getData() != null) builder.setData(entity.getData());
        if (entity.getDescription() != null) builder.setDescription(entity.getDescription());
        if (entity.getType() != null) builder.setType(entity.getType());
        if (entity.getCreatedAt() != null) builder.setCreatedAt(toTimestamp(entity.getCreatedAt()));
        if (entity.getUpdatedAt() != null) builder.setUpdatedAt(toTimestamp(entity.getUpdatedAt()));
        return builder.build();
    }

    // gRPC to DTO
    default SystemParameterResponseDto toResponseDto(SystemParameterData grpc) {
        if (grpc == null) {
            return null;
        }
        return new SystemParameterResponseDto(
                grpc.getId(),
                grpc.getVariable(),
                grpc.getData(),
                grpc.getDescription(),
                grpc.getType(),
                toInstant(grpc.getCreatedAt()),
                toInstant(grpc.getUpdatedAt())
        );
    }

    // DTO to Entity
    default SystemParameter toEntity(UpsertSystemParameterRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return SystemParameter.builder()
                .variable(dto.variable())
                .data(dto.data())
                .description(dto.description())
                .type(dto.type() != null ? dto.type() : "STRING")
                .build();
    }

    // gRPC to Entity
    default SystemParameter toEntity(UpsertSystemParameterRequest request) {
        if (request == null) {
            return null;
        }
        return SystemParameter.builder()
                .variable(request.getVariable())
                .data(request.getData())
                .description(request.getDescription())
                .type(request.getType())
                .build();
    }

    // DTO to gRPC Request
    default UpsertSystemParameterRequest toUpsertRequestGrpc(UpsertSystemParameterRequestDto dto) {
        UpsertSystemParameterRequest.Builder builder = UpsertSystemParameterRequest.newBuilder()
                .setVariable(dto.variable())
                .setData(dto.data());
        if (dto.description() != null) builder.setDescription(dto.description());
        if (dto.type() != null) builder.setType(dto.type());
        return builder.build();
    }

    default OneSystemParameterRequest toOneRequestGrpc(String variable) {
        return OneSystemParameterRequest.newBuilder()
                .setVariable(variable)
                .build();
    }

    // Multiple response mapping
    default ListOfSystemParameterResponseDto toResponseDto(MultipleSystemParameterResponse grpc) {
        List<SystemParameterResponseDto> data = grpc.getDataList().stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
        return new ListOfSystemParameterResponseDto(data, grpc.getNextToken(), grpc.getTotal());
    }
}




