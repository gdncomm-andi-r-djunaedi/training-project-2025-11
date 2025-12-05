package com.gdn.project.waroenk.cart.mapper;

import com.gdn.project.waroenk.cart.*;
import com.gdn.project.waroenk.cart.dto.checkout.*;
import com.gdn.project.waroenk.cart.entity.Checkout;
import com.gdn.project.waroenk.cart.entity.CheckoutItem;
import org.apache.commons.lang3.ObjectUtils;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface CheckoutMapper extends GenericMapper {

    CheckoutMapper INSTANCE = Mappers.getMapper(CheckoutMapper.class);

    // Entity to DTO
    default CheckoutResponseDto toResponseDto(Checkout entity) {
        if (entity == null) {
            return null;
        }
        List<CheckoutItemDto> items = entity.getItems() != null ?
                entity.getItems().stream().map(this::toCheckoutItemDto).collect(Collectors.toList()) :
                new ArrayList<>();
        
        return new CheckoutResponseDto(
                entity.getCheckoutId(),
                entity.getUserId(),
                entity.getSourceCartId(),
                items,
                entity.getTotalAmount(),
                entity.getStatus(),
                entity.getLockedAt(),
                entity.getExpiresAt(),
                entity.getCreatedAt()
        );
    }

    default CheckoutItemDto toCheckoutItemDto(CheckoutItem item) {
        if (item == null) {
            return null;
        }
        return new CheckoutItemDto(
                item.getSku(),
                item.getQuantity(),
                item.getPriceSnapshot(),
                item.getTitle(),
                item.getReserved()
        );
    }

    // Entity to gRPC
    default CheckoutData toResponseGrpc(Checkout entity) {
        if (entity == null) {
            return null;
        }
        CheckoutData.Builder builder = CheckoutData.newBuilder();
        if (entity.getCheckoutId() != null) builder.setCheckoutId(entity.getCheckoutId());
        if (entity.getUserId() != null) builder.setUserId(entity.getUserId());
        if (entity.getSourceCartId() != null) builder.setSourceCartId(entity.getSourceCartId());
        if (entity.getTotalAmount() != null) builder.setTotalAmount(entity.getTotalAmount());
        if (entity.getStatus() != null) builder.setStatus(entity.getStatus());
        if (entity.getLockedAt() != null) builder.setLockedAt(toTimestamp(entity.getLockedAt()));
        if (entity.getExpiresAt() != null) builder.setExpiresAt(toTimestamp(entity.getExpiresAt()));
        if (entity.getCreatedAt() != null) builder.setCreatedAt(toTimestamp(entity.getCreatedAt()));
        
        if (entity.getItems() != null) {
            entity.getItems().forEach(item -> builder.addItems(toCheckoutItemDataGrpc(item)));
        }
        
        return builder.build();
    }

    default CheckoutItemData toCheckoutItemDataGrpc(CheckoutItem item) {
        if (item == null) {
            return null;
        }
        CheckoutItemData.Builder builder = CheckoutItemData.newBuilder();
        if (item.getSku() != null) builder.setSku(item.getSku());
        if (item.getQuantity() != null) builder.setQuantity(item.getQuantity());
        if (item.getPriceSnapshot() != null) builder.setPriceSnapshot(item.getPriceSnapshot());
        if (item.getTitle() != null) builder.setTitle(item.getTitle());
        if (item.getReserved() != null) builder.setReserved(item.getReserved());
        return builder.build();
    }

    // gRPC to DTO
    default CheckoutResponseDto toResponseDto(CheckoutData grpc) {
        if (grpc == null) {
            return null;
        }
        List<CheckoutItemDto> items = grpc.getItemsList().stream()
                .map(this::toCheckoutItemDto)
                .collect(Collectors.toList());
        
        return new CheckoutResponseDto(
                grpc.getCheckoutId(),
                grpc.getUserId(),
                grpc.getSourceCartId(),
                items,
                grpc.getTotalAmount(),
                grpc.getStatus(),
                toInstant(grpc.getLockedAt()),
                toInstant(grpc.getExpiresAt()),
                toInstant(grpc.getCreatedAt())
        );
    }

    default CheckoutItemDto toCheckoutItemDto(CheckoutItemData grpc) {
        if (grpc == null) {
            return null;
        }
        return new CheckoutItemDto(
                grpc.getSku(),
                grpc.getQuantity(),
                grpc.getPriceSnapshot(),
                grpc.getTitle(),
                grpc.getReserved()
        );
    }

    // Validate response mapping
    default ValidateCheckoutResponseDto toValidateResponseDto(ValidateCheckoutResponse grpc) {
        if (grpc == null) {
            return null;
        }
        CheckoutResponseDto checkout = ObjectUtils.isNotEmpty(grpc.getCheckout()) ? toResponseDto(grpc.getCheckout()) : null;
        List<ValidateCheckoutResponseDto.ValidationErrorDto> errors = grpc.getErrorsList().stream()
                .map(e -> new ValidateCheckoutResponseDto.ValidationErrorDto(e.getSku(), e.getErrorCode(), e.getMessage()))
                .collect(Collectors.toList());
        
        return new ValidateCheckoutResponseDto(checkout, grpc.getSuccess(), grpc.getMessage(), errors);
    }

    default ValidateCheckoutResponse toValidateResponseGrpc(ValidateCheckoutResponseDto dto) {
        if (dto == null) {
            return null;
        }
        ValidateCheckoutResponse.Builder builder = ValidateCheckoutResponse.newBuilder()
                .setSuccess(dto.success())
                .setMessage(dto.message() != null ? dto.message() : "");
        
        if (dto.checkout() != null) {
            // Convert CheckoutResponseDto to CheckoutData gRPC
            builder.setCheckout(toCheckoutDataGrpc(dto.checkout()));
        }
        
        if (dto.errors() != null) {
            dto.errors().forEach(e -> builder.addErrors(
                    ValidationError.newBuilder()
                            .setSku(e.sku())
                            .setErrorCode(e.errorCode())
                            .setMessage(e.message())
                            .build()
            ));
        }
        
        return builder.build();
    }

    default CheckoutData toCheckoutDataGrpc(CheckoutResponseDto dto) {
        if (dto == null) {
            return null;
        }
        CheckoutData.Builder builder = CheckoutData.newBuilder();
        if (dto.checkoutId() != null) builder.setCheckoutId(dto.checkoutId());
        if (dto.userId() != null) builder.setUserId(dto.userId());
        if (dto.sourceCartId() != null) builder.setSourceCartId(dto.sourceCartId());
        if (dto.totalAmount() != null) builder.setTotalAmount(dto.totalAmount());
        if (dto.status() != null) builder.setStatus(dto.status());
        if (dto.lockedAt() != null) builder.setLockedAt(toTimestamp(dto.lockedAt()));
        if (dto.expiresAt() != null) builder.setExpiresAt(toTimestamp(dto.expiresAt()));
        if (dto.createdAt() != null) builder.setCreatedAt(toTimestamp(dto.createdAt()));
        
        if (dto.items() != null) {
            dto.items().forEach(item -> {
                CheckoutItemData.Builder itemBuilder = CheckoutItemData.newBuilder();
                if (item.sku() != null) itemBuilder.setSku(item.sku());
                if (item.quantity() != null) itemBuilder.setQuantity(item.quantity());
                if (item.priceSnapshot() != null) itemBuilder.setPriceSnapshot(item.priceSnapshot());
                if (item.title() != null) itemBuilder.setTitle(item.title());
                if (item.reserved() != null) itemBuilder.setReserved(item.reserved());
                builder.addItems(itemBuilder.build());
            });
        }
        
        return builder.build();
    }

    // Finalize response mapping
    default FinalizeCheckoutResponseDto toFinalizeResponseDto(FinalizeCheckoutResponse grpc) {
        if (grpc == null) {
            return null;
        }
        return new FinalizeCheckoutResponseDto(grpc.getSuccess(), grpc.getMessage(), grpc.getOrderId());
    }

    default FinalizeCheckoutResponse toFinalizeResponseGrpc(FinalizeCheckoutResponseDto dto) {
        if (dto == null) {
            return null;
        }
        return FinalizeCheckoutResponse.newBuilder()
                .setSuccess(dto.success())
                .setMessage(dto.message() != null ? dto.message() : "")
                .setOrderId(dto.orderId() != null ? dto.orderId() : "")
                .build();
    }

    // DTO to gRPC Request
    default ValidateCheckoutRequest toValidateRequestGrpc(ValidateCheckoutRequestDto dto) {
        return ValidateCheckoutRequest.newBuilder()
                .setUserId(dto.userId())
                .build();
    }

    default InvalidateCheckoutRequest toInvalidateRequestGrpc(InvalidateCheckoutRequestDto dto) {
        return InvalidateCheckoutRequest.newBuilder()
                .setCheckoutId(dto.checkoutId())
                .build();
    }

    default FinalizeCheckoutRequest toFinalizeRequestGrpc(FinalizeCheckoutRequestDto dto) {
        return FinalizeCheckoutRequest.newBuilder()
                .setCheckoutId(dto.checkoutId())
                .setOrderId(dto.orderId())
                .build();
    }
}




