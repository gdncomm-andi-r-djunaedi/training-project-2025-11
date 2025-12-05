package com.gdn.project.waroenk.cart.mapper;

import com.gdn.project.waroenk.cart.*;
import com.gdn.project.waroenk.cart.dto.cart.*;
import com.gdn.project.waroenk.cart.entity.Cart;
import com.gdn.project.waroenk.cart.entity.CartItem;
import com.google.protobuf.Timestamp;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper
public interface CartMapper extends GenericMapper {

    CartMapper INSTANCE = Mappers.getMapper(CartMapper.class);

    // Entity to DTO
    default CartResponseDto toResponseDto(Cart entity) {
        if (entity == null) {
            return null;
        }
        List<CartItemDto> items = entity.getItems() != null ?
                entity.getItems().stream().map(this::toCartItemDto).collect(Collectors.toList()) :
                new ArrayList<>();
        
        return new CartResponseDto(
                entity.getId(),
                entity.getUserId(),
                items,
                entity.getCurrency(),
                entity.getTotalAmount(),
                entity.getTotalItems(),
                entity.getVersion(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    default CartItemDto toCartItemDto(CartItem item) {
        if (item == null) {
            return null;
        }
        return new CartItemDto(
                item.getSku(),
                item.getQuantity(),
                item.getPriceSnapshot(),
                item.getTitle(),
                item.getImageUrl(),
                item.getAttributes()
        );
    }

    // Entity to gRPC
    default CartData toResponseGrpc(Cart entity) {
        if (entity == null) {
            return null;
        }
        CartData.Builder builder = CartData.newBuilder();
        if (entity.getId() != null) builder.setId(entity.getId());
        if (entity.getUserId() != null) builder.setUserId(entity.getUserId());
        if (entity.getCurrency() != null) builder.setCurrency(entity.getCurrency());
        builder.setTotalAmount(entity.getTotalAmount());
        builder.setTotalItems(entity.getTotalItems());
        if (entity.getVersion() != null) builder.setVersion(entity.getVersion());
        if (entity.getCreatedAt() != null) builder.setCreatedAt(toTimestamp(entity.getCreatedAt()));
        if (entity.getUpdatedAt() != null) builder.setUpdatedAt(toTimestamp(entity.getUpdatedAt()));
        
        if (entity.getItems() != null) {
            entity.getItems().forEach(item -> builder.addItems(toCartItemDataGrpc(item)));
        }
        
        return builder.build();
    }

    default CartItemData toCartItemDataGrpc(CartItem item) {
        if (item == null) {
            return null;
        }
        CartItemData.Builder builder = CartItemData.newBuilder();
        if (item.getSku() != null) builder.setSku(item.getSku());
        if (item.getQuantity() != null) builder.setQuantity(item.getQuantity());
        if (item.getPriceSnapshot() != null) builder.setPriceSnapshot(item.getPriceSnapshot());
        if (item.getTitle() != null) builder.setTitle(item.getTitle());
        if (item.getImageUrl() != null) builder.setImageUrl(item.getImageUrl());
        if (item.getAttributes() != null) builder.putAllAttributes(item.getAttributes());
        return builder.build();
    }

    // gRPC to Entity
    default CartItem toCartItemEntity(AddCartItemRequest request) {
        if (request == null) {
            return null;
        }
        Map<String, String> attrs = new HashMap<>(request.getAttributesMap());
        return CartItem.builder()
                .sku(request.getSku())
                .quantity(request.getQuantity())
                .priceSnapshot(request.getPriceSnapshot())
                .title(request.getTitle())
                .imageUrl(request.getImageUrl())
                .attributes(attrs)
                .build();
    }

    default CartItem toCartItemEntity(CartItemInput input) {
        if (input == null) {
            return null;
        }
        Map<String, String> attrs = new HashMap<>(input.getAttributesMap());
        return CartItem.builder()
                .sku(input.getSku())
                .quantity(input.getQuantity())
                .priceSnapshot(input.getPriceSnapshot())
                .title(input.getTitle())
                .imageUrl(input.getImageUrl())
                .attributes(attrs)
                .build();
    }

    // DTO to Entity
    default CartItem toCartItemEntity(AddCartItemRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return CartItem.builder()
                .sku(dto.sku())
                .quantity(dto.quantity())
                .priceSnapshot(dto.priceSnapshot())
                .title(dto.title())
                .imageUrl(dto.imageUrl())
                .attributes(dto.attributes())
                .build();
    }

    default CartItem toCartItemEntity(BulkAddCartItemsRequestDto.CartItemInputDto dto) {
        if (dto == null) {
            return null;
        }
        return CartItem.builder()
                .sku(dto.sku())
                .quantity(dto.quantity())
                .priceSnapshot(dto.priceSnapshot())
                .title(dto.title())
                .imageUrl(dto.imageUrl())
                .attributes(dto.attributes())
                .build();
    }

    // DTO to gRPC Request
    default AddCartItemRequest toAddItemRequestGrpc(AddCartItemRequestDto dto) {
        AddCartItemRequest.Builder builder = AddCartItemRequest.newBuilder()
                .setUserId(dto.userId())
                .setSku(dto.sku())
                .setQuantity(dto.quantity());
        if (dto.priceSnapshot() != null) builder.setPriceSnapshot(dto.priceSnapshot());
        if (dto.title() != null) builder.setTitle(dto.title());
        if (dto.imageUrl() != null) builder.setImageUrl(dto.imageUrl());
        if (dto.attributes() != null) builder.putAllAttributes(dto.attributes());
        return builder.build();
    }

    default BulkAddCartItemsRequest toBulkAddRequestGrpc(BulkAddCartItemsRequestDto dto) {
        BulkAddCartItemsRequest.Builder builder = BulkAddCartItemsRequest.newBuilder()
                .setUserId(dto.userId());
        if (dto.items() != null) {
            dto.items().forEach(item -> {
                CartItemInput.Builder itemBuilder = CartItemInput.newBuilder()
                        .setSku(item.sku())
                        .setQuantity(item.quantity());
                if (item.priceSnapshot() != null) itemBuilder.setPriceSnapshot(item.priceSnapshot());
                if (item.title() != null) itemBuilder.setTitle(item.title());
                if (item.imageUrl() != null) itemBuilder.setImageUrl(item.imageUrl());
                if (item.attributes() != null) itemBuilder.putAllAttributes(item.attributes());
                builder.addItems(itemBuilder.build());
            });
        }
        return builder.build();
    }

    default RemoveCartItemRequest toRemoveItemRequestGrpc(RemoveCartItemRequestDto dto) {
        return RemoveCartItemRequest.newBuilder()
                .setUserId(dto.userId())
                .setSku(dto.sku())
                .build();
    }

    default BulkRemoveCartItemsRequest toBulkRemoveRequestGrpc(BulkRemoveCartItemsRequestDto dto) {
        return BulkRemoveCartItemsRequest.newBuilder()
                .setUserId(dto.userId())
                .addAllSkus(dto.skus())
                .build();
    }

    default UpdateCartItemRequest toUpdateItemRequestGrpc(UpdateCartItemRequestDto dto) {
        return UpdateCartItemRequest.newBuilder()
                .setUserId(dto.userId())
                .setSku(dto.sku())
                .setQuantity(dto.quantity())
                .build();
    }

    // gRPC to DTO
    default CartResponseDto toResponseDto(CartData grpc) {
        if (grpc == null) {
            return null;
        }
        List<CartItemDto> items = grpc.getItemsList().stream()
                .map(this::toCartItemDto)
                .collect(Collectors.toList());
        
        return new CartResponseDto(
                grpc.getId(),
                grpc.getUserId(),
                items,
                grpc.getCurrency(),
                grpc.getTotalAmount(),
                grpc.getTotalItems(),
                grpc.getVersion(),
                toInstant(grpc.getCreatedAt()),
                toInstant(grpc.getUpdatedAt())
        );
    }

    default CartItemDto toCartItemDto(CartItemData grpc) {
        if (grpc == null) {
            return null;
        }
        return new CartItemDto(
                grpc.getSku(),
                grpc.getQuantity(),
                grpc.getPriceSnapshot(),
                grpc.getTitle(),
                grpc.getImageUrl(),
                new HashMap<>(grpc.getAttributesMap())
        );
    }

    default ListOfCartResponseDto toResponseDto(MultipleCartResponse grpc) {
        List<CartResponseDto> data = grpc.getDataList().stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
        return new ListOfCartResponseDto(data, grpc.getNextToken(), grpc.getTotal());
    }
}




