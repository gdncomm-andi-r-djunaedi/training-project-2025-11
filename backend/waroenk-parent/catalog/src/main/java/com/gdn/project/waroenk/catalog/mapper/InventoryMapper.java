package com.gdn.project.waroenk.catalog.mapper;

import com.gdn.project.waroenk.catalog.BulkUpdateStockRequest;
import com.gdn.project.waroenk.catalog.BulkUpdateStockResponse;
import com.gdn.project.waroenk.catalog.CreateInventoryRequest;
import com.gdn.project.waroenk.catalog.InventoryData;
import com.gdn.project.waroenk.catalog.MultipleInventoryResponse;
import com.gdn.project.waroenk.catalog.UpdateInventoryRequest;
import com.gdn.project.waroenk.catalog.UpdateStockBySubSkuRequest;
import com.gdn.project.waroenk.catalog.dto.inventory.BulkUpdateStockRequestDto;
import com.gdn.project.waroenk.catalog.dto.inventory.BulkUpdateStockResponseDto;
import com.gdn.project.waroenk.catalog.dto.inventory.CreateInventoryRequestDto;
import com.gdn.project.waroenk.catalog.dto.inventory.InventoryResponseDto;
import com.gdn.project.waroenk.catalog.dto.inventory.ListOfInventoryResponseDto;
import com.gdn.project.waroenk.catalog.dto.inventory.UpdateInventoryRequestDto;
import com.gdn.project.waroenk.catalog.entity.Inventory;
import com.google.protobuf.Timestamp;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.List;

@Mapper
public interface InventoryMapper extends GenericMapper {

  InventoryMapper INSTANCE = Mappers.getMapper(InventoryMapper.class);

  InventoryResponseDto toResponseDto(InventoryData grpc);

  InventoryResponseDto toResponseDto(Inventory entity);

  default InventoryData toResponseGrpc(Inventory entity) {
    InventoryData.Builder builder = InventoryData.newBuilder();
    builder.setId(entity.getId());
    builder.setSubSku(entity.getSubSku());
    builder.setStock(entity.getStock());
    if (entity.getCreatedAt() != null) {
      builder.setCreatedAt(toTimestamp(entity.getCreatedAt()));
    }
    if (entity.getUpdatedAt() != null) {
      builder.setUpdatedAt(toTimestamp(entity.getUpdatedAt()));
    }
    return builder.build();
  }

  default Inventory toEntity(CreateInventoryRequest request) {
    return Inventory.builder()
        .subSku(request.getSubSku())
        .stock(request.getStock())
        .build();
  }

  default Inventory toEntity(CreateInventoryRequestDto dto) {
    return Inventory.builder()
        .subSku(dto.subSku())
        .stock(dto.stock())
        .build();
  }

  default ListOfInventoryResponseDto toResponseDto(MultipleInventoryResponse grpc) {
    String token = grpc.getNextToken();
    Integer total = grpc.getTotal();
    List<InventoryData> data = grpc.getDataList();
    return new ListOfInventoryResponseDto(data.stream().map(this::toResponseDto).toList(), token, total);
  }

  default BulkUpdateStockResponseDto toResponseDto(BulkUpdateStockResponse grpc) {
    List<InventoryResponseDto> data = grpc.getDataList().stream().map(this::toResponseDto).toList();
    return new BulkUpdateStockResponseDto(data, grpc.getSuccessCount(), grpc.getFailureCount());
  }

  default CreateInventoryRequest toRequestGrpc(CreateInventoryRequestDto dto) {
    return CreateInventoryRequest.newBuilder()
        .setSubSku(dto.subSku())
        .setStock(dto.stock())
        .build();
  }

  default UpdateInventoryRequest toRequestGrpc(String id, UpdateInventoryRequestDto dto) {
    UpdateInventoryRequest.Builder builder = UpdateInventoryRequest.newBuilder();
    builder.setId(id);
    if (dto.subSku() != null) builder.setSubSku(dto.subSku());
    if (dto.stock() != null) builder.setStock(dto.stock());
    return builder.build();
  }

  default BulkUpdateStockRequest toRequestGrpc(BulkUpdateStockRequestDto dto) {
    BulkUpdateStockRequest.Builder builder = BulkUpdateStockRequest.newBuilder();
    if (dto.items() != null) {
      for (BulkUpdateStockRequestDto.StockUpdateItem item : dto.items()) {
        builder.addItems(UpdateStockBySubSkuRequest.newBuilder()
            .setSubSku(item.subSku())
            .setStock(item.stock())
            .build());
      }
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






