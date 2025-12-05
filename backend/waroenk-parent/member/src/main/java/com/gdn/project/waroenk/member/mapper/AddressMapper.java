package com.gdn.project.waroenk.member.mapper;

import com.gdn.project.waroenk.member.AddressData;
import com.gdn.project.waroenk.member.MultipleAddressResponse;
import com.gdn.project.waroenk.member.UpsertAddressRequest;
import com.gdn.project.waroenk.member.dto.AddressResponseDto;
import com.gdn.project.waroenk.member.dto.ListOfAddressResponseDto;
import com.gdn.project.waroenk.member.dto.UpsertAddressRequestDto;
import com.gdn.project.waroenk.member.entity.Address;
import com.google.protobuf.Timestamp;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Mapper
public interface AddressMapper extends GenericMapper {

  AddressMapper INSTANCE = Mappers.getMapper(AddressMapper.class);

  default AddressData toAddressData(Address address) {
    if (address == null) {
      return null;
    }

    AddressData.Builder builder = AddressData.newBuilder();

    if (address.getId() != null) {
      builder.setId(address.getId().toString());
    }
    if (address.getLabel() != null) {
      builder.setLabel(address.getLabel());
    }
    if (address.getCountry() != null) {
      builder.setCountry(address.getCountry());
    }
    if (address.getProvince() != null) {
      builder.setProvince(address.getProvince());
    }
    if (address.getCity() != null) {
      builder.setCity(address.getCity());
    }
    if (address.getDistrict() != null) {
      builder.setDistrict(address.getDistrict());
    }
    if (address.getSubdistrict() != null) {
      builder.setSubDistrict(address.getSubdistrict());
    }
    if (address.getPostalCode() != null) {
      builder.setPostalCode(address.getPostalCode());
    }
    if (address.getStreet() != null) {
      builder.setStreet(address.getStreet());
    }
    if (address.getLatitude() != null) {
      builder.setLatitude(address.getLatitude().floatValue());
    }
    if (address.getLongitude() != null) {
      builder.setLongitude(address.getLongitude().floatValue());
    }
    if (address.getDetails() != null) {
      builder.setDetails(address.getDetails());
    }
    if (address.getCreatedAt() != null) {
      builder.setCreatedAt(localDateTimeToTimestamp(address.getCreatedAt()));
    }
    if (address.getUpdatedAt() != null) {
      builder.setUpdatedAt(localDateTimeToTimestamp(address.getUpdatedAt()));
    }

    return builder.build();
  }

  default AddressResponseDto toAddressResponseDto(Address address) {
    if (address == null) {
      return null;
    }
    return new AddressResponseDto(
        address.getId(),
        address.getLabel(),
        address.getLongitude() != null ? address.getLongitude().floatValue() : null,
        address.getLatitude() != null ? address.getLatitude().floatValue() : null,
        address.getCountry(),
        address.getPostalCode(),
        address.getProvince(),
        address.getCity(),
        address.getDistrict(),
        address.getSubdistrict(),
        address.getStreet(),
        address.getDetails(),
        address.getCreatedAt(),
        address.getUpdatedAt()
    );
  }

  default AddressResponseDto toAddressResponseDto(AddressData grpc) {
    if (grpc == null) {
      return null;
    }
    return new AddressResponseDto(
        grpc.getId().isEmpty() ? null : java.util.UUID.fromString(grpc.getId()),
        grpc.getLabel(),
        grpc.getLongitude(),
        grpc.getLatitude(),
        grpc.getCountry(),
        grpc.getPostalCode(),
        grpc.getProvince(),
        grpc.getCity(),
        grpc.getDistrict(),
        grpc.getSubDistrict(),
        grpc.getStreet(),
        grpc.getDetails(),
        toLocalDateTime(grpc.getCreatedAt()),
        toLocalDateTime(grpc.getUpdatedAt())
    );
  }

  default ListOfAddressResponseDto toListOfAddressResponseDto(MultipleAddressResponse grpc) {
    List<AddressData> data = grpc.getDataList();
    return new ListOfAddressResponseDto(
        data.stream().map(this::toAddressResponseDto).toList(),
        grpc.getNextToken(),
        grpc.getTotal()
    );
  }

  default Address toAddressEntity(UpsertAddressRequest grpc) {
    if (grpc == null) {
      return null;
    }
    return Address.builder()
        .label(grpc.getLabel())
        .country(grpc.getCountry())
        .province(grpc.getProvince())
        .city(grpc.getCity())
        .district(grpc.getDistrict())
        .subdistrict(grpc.getSubDistrict())
        .postalCode(grpc.getPostalCode())
        .street(grpc.getStreet())
        .latitude(grpc.getLatitude() != 0 ? BigDecimal.valueOf(grpc.getLatitude()) : null)
        .longitude(grpc.getLongitude() != 0 ? BigDecimal.valueOf(grpc.getLongitude()) : null)
        .details(grpc.getDetails())
        .build();
  }

  default Address toAddressEntity(UpsertAddressRequestDto dto) {
    if (dto == null) {
      return null;
    }
    return Address.builder()
        .label(dto.label())
        .country(dto.country())
        .province(dto.province())
        .city(dto.city())
        .district(dto.district())
        .subdistrict(dto.subDistrict())
        .postalCode(dto.postalCode())
        .street(dto.street())
        .latitude(dto.latitude() != null ? BigDecimal.valueOf(dto.latitude()) : null)
        .longitude(dto.longitude() != null ? BigDecimal.valueOf(dto.longitude()) : null)
        .details(dto.details())
        .build();
  }

  default UpsertAddressRequest toUpsertAddressRequestGrpc(UpsertAddressRequestDto dto) {
    if (dto == null) {
      return null;
    }
    UpsertAddressRequest.Builder builder = UpsertAddressRequest.newBuilder();
    if (dto.userId() != null) {
      builder.setUserId(dto.userId());
    }
    if (dto.label() != null) {
      builder.setLabel(dto.label());
    }
    if (dto.country() != null) {
      builder.setCountry(dto.country());
    }
    if (dto.province() != null) {
      builder.setProvince(dto.province());
    }
    if (dto.city() != null) {
      builder.setCity(dto.city());
    }
    if (dto.district() != null) {
      builder.setDistrict(dto.district());
    }
    if (dto.subDistrict() != null) {
      builder.setSubDistrict(dto.subDistrict());
    }
    if (dto.postalCode() != null) {
      builder.setPostalCode(dto.postalCode());
    }
    if (dto.street() != null) {
      builder.setStreet(dto.street());
    }
    if (dto.latitude() != null) {
      builder.setLatitude(dto.latitude());
    }
    if (dto.longitude() != null) {
      builder.setLongitude(dto.longitude());
    }
    if (dto.details() != null) {
      builder.setDetails(dto.details());
    }
    return builder.build();
  }

  default Timestamp localDateTimeToTimestamp(LocalDateTime localDateTime) {
    if (localDateTime == null) {
      return null;
    }
    Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
    return Timestamp.newBuilder()
        .setSeconds(instant.getEpochSecond())
        .setNanos(instant.getNano())
        .build();
  }

  default LocalDateTime toLocalDateTime(Timestamp value) {
    if (value == null || (value.getSeconds() == 0 && value.getNanos() == 0)) {
      return null;
    }
    return LocalDateTime.ofInstant(
        Instant.ofEpochSecond(value.getSeconds(), value.getNanos()),
        ZoneId.systemDefault()
    );
  }
}







