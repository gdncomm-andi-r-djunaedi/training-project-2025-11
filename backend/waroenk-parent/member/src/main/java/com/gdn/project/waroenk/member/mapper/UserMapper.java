package com.gdn.project.waroenk.member.mapper;

import com.gdn.project.waroenk.member.AddressData;
import com.gdn.project.waroenk.member.ChangePasswordRequest;
import com.gdn.project.waroenk.member.ChangePasswordResponse;
import com.gdn.project.waroenk.member.CreateUserRequest;
import com.gdn.project.waroenk.member.CreateUserResponse;
import com.gdn.project.waroenk.member.ForgotPasswordRequest;
import com.gdn.project.waroenk.member.ForgotPasswordResponse;
import com.gdn.project.waroenk.member.LogoutRequest;
import com.gdn.project.waroenk.member.LogoutResponse;
import com.gdn.project.waroenk.member.MultipleUserResponse;
import com.gdn.project.waroenk.member.RefreshTokenRequest;
import com.gdn.project.waroenk.member.RefreshTokenResponse;
import com.gdn.project.waroenk.member.UpdateUserRequest;
import com.gdn.project.waroenk.member.UserData;
import com.gdn.project.waroenk.member.UserTokenResponse;
import com.gdn.project.waroenk.member.constant.Gender;
import com.gdn.project.waroenk.member.dto.AddressResponseDto;
import com.gdn.project.waroenk.member.dto.ChangePasswordRequestDto;
import com.gdn.project.waroenk.member.dto.ChangePasswordResponseDto;
import com.gdn.project.waroenk.member.dto.CreateUserRequestDto;
import com.gdn.project.waroenk.member.dto.ForgotPasswordRequestDto;
import com.gdn.project.waroenk.member.dto.ForgotPasswordResponseDto;
import com.gdn.project.waroenk.member.dto.ListOfUserResponseDto;
import com.gdn.project.waroenk.member.dto.LogoutRequestDto;
import com.gdn.project.waroenk.member.dto.LogoutResponseDto;
import com.gdn.project.waroenk.member.dto.RefreshTokenRequestDto;
import com.gdn.project.waroenk.member.dto.RefreshTokenResponseDto;
import com.gdn.project.waroenk.member.dto.UpdateUserRequestDto;
import com.gdn.project.waroenk.member.dto.UserResponseDto;
import com.gdn.project.waroenk.member.dto.UserTokenResponseDto;
import com.gdn.project.waroenk.member.entity.Address;
import com.gdn.project.waroenk.member.entity.User;
import com.google.protobuf.Timestamp;
import com.google.type.Date;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Mapper
public interface UserMapper extends GenericMapper {

  UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  /**
   * Convert User entity to UserData gRPC (with address)
   */
  default UserData toUserData(User user) {
    if (user == null) {
      return null;
    }

    UserData.Builder builder = UserData.newBuilder();

    if (user.getId() != null) {
      builder.setId(user.getId().toString());
    }
    if (user.getFullName() != null) {
      builder.setFullName(user.getFullName());
    }
    if (user.getEmail() != null) {
      builder.setEmail(user.getEmail());
    }
    if (user.getPhoneNumber() != null) {
      builder.setPhone(user.getPhoneNumber());
    }
    if (user.getDob() != null) {
      builder.setDob(localDateToProtoDate(user.getDob()));
    }
    if (user.getDefaultAddress() != null) {
      builder.setDefaultAddress(addressToAddressData(user.getDefaultAddress()));
    }
    if (user.getGender() != null) {
      builder.setGender(user.getGender().getGender());
    }
    if (user.getCreatedAt() != null) {
      builder.setCreatedAt(localDateTimeToTimestamp(user.getCreatedAt()));
    }
    if (user.getUpdatedAt() != null) {
      builder.setUpdatedAt(localDateTimeToTimestamp(user.getUpdatedAt()));
    }

    return builder.build();
  }

  /**
   * Convert User entity to UserData gRPC (without address)
   */
  default UserData toUserDataWithoutAddress(User user) {
    if (user == null) {
      return null;
    }

    UserData.Builder builder = UserData.newBuilder();

    if (user.getId() != null) {
      builder.setId(user.getId().toString());
    }
    if (user.getFullName() != null) {
      builder.setFullName(user.getFullName());
    }
    if (user.getEmail() != null) {
      builder.setEmail(user.getEmail());
    }
    if (user.getPhoneNumber() != null) {
      builder.setPhone(user.getPhoneNumber());
    }
    if (user.getDob() != null) {
      builder.setDob(localDateToProtoDate(user.getDob()));
    }
    // Note: defaultAddress is intentionally NOT mapped
    if (user.getGender() != null) {
      builder.setGender(user.getGender().getGender());
    }
    if (user.getCreatedAt() != null) {
      builder.setCreatedAt(localDateTimeToTimestamp(user.getCreatedAt()));
    }
    if (user.getUpdatedAt() != null) {
      builder.setUpdatedAt(localDateTimeToTimestamp(user.getUpdatedAt()));
    }

    return builder.build();
  }

  default Timestamp instantToTimestamp(Instant instant) {
    if (instant == null) {
      return null;
    }
    return Timestamp.newBuilder()
        .setSeconds(instant.getEpochSecond())
        .setNanos(instant.getNano())
        .build();
  }

  default Timestamp localDateTimeToTimestamp(LocalDateTime localDateTime) {
    if (localDateTime == null) {
      return null;
    }
    // Convert LocalDateTime to Instant using system timezone
    Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
    return Timestamp.newBuilder()
        .setSeconds(instant.getEpochSecond())
        .setNanos(instant.getNano())
        .build();
  }

  default Date localDateToProtoDate(LocalDate localDate) {
    if (localDate == null) {
      return null;
    }
    return Date.newBuilder()
        .setYear(localDate.getYear())
        .setMonth(localDate.getMonthValue())
        .setDay(localDate.getDayOfMonth())
        .build();
  }

  default AddressData addressToAddressData(Address address) {
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

  /**
   * Convert User entity to CreateUserResponse gRPC
   */
  default CreateUserResponse toCreateUserResponse(User user) {
    if (user == null) {
      return null;
    }

    CreateUserResponse.Builder builder = CreateUserResponse.newBuilder();

    if (user.getId() != null) {
      builder.setId(user.getId().toString());
    }
    if (user.getFullName() != null) {
      builder.setFullName(user.getFullName());
    }
    if (user.getEmail() != null) {
      builder.setEmail(user.getEmail());
    }
    if (user.getPhoneNumber() != null) {
      builder.setPhone(user.getPhoneNumber());
    }
    if (user.getDob() != null) {
      builder.setDob(localDateToProtoDate(user.getDob()));
    }
    if (user.getGender() != null) {
      builder.setGender(user.getGender().getGender());
    }
    if (user.getCreatedAt() != null) {
      builder.setCreatedAt(localDateTimeToTimestamp(user.getCreatedAt()));
    }

    return builder.build();
  }

  /**
   * Convert CreateUserRequest gRPC to User entity
   */
  default User toUserEntity(CreateUserRequest request) {
    if (request == null) {
      return null;
    }

    User.UserBuilder builder = User.builder();
    builder.fullName(request.getFullName());
    builder.email(request.getEmail());
    builder.phoneNumber(request.getPhone());

    if (ObjectUtils.isNotEmpty(request.getDob())) {
      builder.dob(protoDateToLocalDate(request.getDob()));
    }

    if (StringUtils.isNotBlank(request.getGender())) {
      builder.gender(parseGender(request.getGender()));
    }

    return builder.build();
  }

  /**
   * Convert CreateUserRequestDto to User entity
   */
  default User toUserEntity(CreateUserRequestDto dto) {
    if (dto == null) {
      return null;
    }

    User.UserBuilder builder = User.builder()
        .fullName(dto.fullName())
        .email(dto.email())
        .phoneNumber(dto.phone());

    if (dto.dob() != null) {
      builder.dob(dto.dob());
    }
    if (StringUtils.isNotBlank(dto.gender())) {
      builder.gender(parseGender(dto.gender()));
    }

    return builder.build();
  }

  /**
   * Convert CreateUserRequestDto to CreateUserRequest gRPC
   */
  default CreateUserRequest toCreateUserRequestGrpc(CreateUserRequestDto dto) {
    if (dto == null) {
      return null;
    }

    CreateUserRequest.Builder builder = CreateUserRequest.newBuilder();
    if (dto.fullName() != null) {
      builder.setFullName(dto.fullName());
    }
    if (dto.email() != null) {
      builder.setEmail(dto.email());
    }
    if (dto.phone() != null) {
      builder.setPhone(dto.phone());
    }
    if (dto.password() != null) {
      builder.setPassword(dto.password());
    }
    if (dto.dob() != null) {
      builder.setDob(localDateToProtoDate(dto.dob()));
    }
    if (dto.gender() != null) {
      builder.setGender(dto.gender());
    }
    return builder.build();
  }

  /**
   * Convert User entity to UserResponseDto
   */
  default UserResponseDto toUserResponseDto(User user) {
    if (user == null) {
      return null;
    }

    AddressResponseDto addressDto = null;
    if (user.getDefaultAddress() != null) {
      Address address = user.getDefaultAddress();
      addressDto = new AddressResponseDto(
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

    return new UserResponseDto(
        user.getId(),
        user.getFullName(),
        user.getDob(),
        user.getEmail(),
        user.getPhoneNumber(),
        user.getGender(),
        addressDto,
        user.getCreatedAt(),
        user.getUpdatedAt()
    );
  }

  /**
   * Convert UserData gRPC to UserResponseDto
   */
  default UserResponseDto toUserResponseDto(UserData grpc) {
    if (grpc == null) {
      return null;
    }

    AddressResponseDto addressDto = null;
    if (ObjectUtils.isNotEmpty(grpc.getDefaultAddress())) {
      AddressData addr = grpc.getDefaultAddress();
      addressDto = new AddressResponseDto(
          addr.getId().isEmpty() ? null : UUID.fromString(addr.getId()),
          addr.getLabel(),
          addr.getLongitude(),
          addr.getLatitude(),
          addr.getCountry(),
          addr.getPostalCode(),
          addr.getProvince(),
          addr.getCity(),
          addr.getDistrict(),
          addr.getSubDistrict(),
          addr.getStreet(),
          addr.getDetails(),
          toLocalDateTime(addr.getCreatedAt()),
          toLocalDateTime(addr.getUpdatedAt())
      );
    }

    return new UserResponseDto(
        grpc.getId().isEmpty() ? null : UUID.fromString(grpc.getId()),
        grpc.getFullName(),
        ObjectUtils.isNotEmpty(grpc.getDob()) ? protoDateToLocalDate(grpc.getDob()) : null,
        grpc.getEmail(),
        grpc.getPhone(),
        StringUtils.isNotBlank(grpc.getGender()) ? parseGender(grpc.getGender()) : null,
        addressDto,
        toLocalDateTime(grpc.getCreatedAt()),
        toLocalDateTime(grpc.getUpdatedAt())
    );
  }

  /**
   * Convert CreateUserResponse gRPC to UserResponseDto
   */
  default UserResponseDto toUserResponseDto(CreateUserResponse grpc) {
    if (grpc == null) {
      return null;
    }

    return new UserResponseDto(
        grpc.getId().isEmpty() ? null : UUID.fromString(grpc.getId()),
        grpc.getFullName(),
        ObjectUtils.isNotEmpty(grpc.getDob()) ? protoDateToLocalDate(grpc.getDob()) : null,
        grpc.getEmail(),
        grpc.getPhone(),
        StringUtils.isNotBlank(grpc.getGender()) ? parseGender(grpc.getGender()) : null,
        null,
        toLocalDateTime(grpc.getCreatedAt()),
        null
    );
  }

  /**
   * Convert MultipleUserResponse gRPC to ListOfUserResponseDto
   */
  default ListOfUserResponseDto toListOfUserResponseDto(MultipleUserResponse grpc) {
    if (grpc == null) {
      return null;
    }
    List<UserData> data = grpc.getDataList();
    return new ListOfUserResponseDto(
        data.stream().map(this::toUserResponseDto).toList(),
        grpc.getNextToken(),
        grpc.getTotal()
    );
  }

  /**
   * Convert UserTokenResponse gRPC to UserTokenResponseDto
   */
  default UserTokenResponseDto toUserTokenResponseDto(UserTokenResponse grpc) {
    if (grpc == null) {
      return null;
    }
    return new UserTokenResponseDto(
        grpc.getAccessToken(),
        grpc.getTokenType(),
        grpc.getExpiresIn(),
        grpc.getUserId()
    );
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

  default LocalDate protoDateToLocalDate(Date date) {
    if (date == null) {
      return null;
    }
    // Check for empty/default protobuf Date (year=0, month=0, day=0)
    if (date.getYear() == 0 && date.getMonth() == 0 && date.getDay() == 0) {
      return null;
    }
    // Validate month and day are within valid ranges
    if (date.getMonth() < 1 || date.getMonth() > 12 || date.getDay() < 1 || date.getDay() > 31) {
      return null;
    }
    return LocalDate.of(date.getYear(), date.getMonth(), date.getDay());
  }

  default Gender parseGender(String gender) {
    if (StringUtils.isBlank(gender)) {
      return null;
    }
    return switch (gender.toUpperCase()) {
      case "M", "MALE" -> Gender.MALE;
      case "F", "FEMALE" -> Gender.FEMALE;
      case "O", "OTHER" -> Gender.OTHER;
      default -> null;
    };
  }

  /**
   * Convert UpdateUserRequest gRPC to User entity
   */
  default User toUserEntity(UpdateUserRequest request) {
    if (request == null) {
      return null;
    }

    User.UserBuilder builder = User.builder();
    builder.id(UUID.fromString(request.getId()));
    builder.fullName(request.getFullName());

    if (StringUtils.isNotBlank(request.getEmail())) {
      builder.email(request.getEmail());
    }
    if (StringUtils.isNotBlank(request.getPhone())) {
      builder.phoneNumber(request.getPhone());
    }
    if (ObjectUtils.isNotEmpty(request.getDob())) {
      builder.dob(protoDateToLocalDate(request.getDob()));
    }
    if (StringUtils.isNotBlank(request.getGender())) {
      builder.gender(parseGender(request.getGender()));
    }

    return builder.build();
  }

  /**
   * Convert UpdateUserRequestDto to User entity
   */
  default User toUserEntityForUpdate(UpdateUserRequestDto dto) {
    if (dto == null) {
      return null;
    }

    User.UserBuilder builder = User.builder();
    builder.id(UUID.fromString(dto.id()));
    builder.fullName(dto.fullName());

    if (StringUtils.isNotBlank(dto.email())) {
      builder.email(dto.email());
    }
    if (StringUtils.isNotBlank(dto.phone())) {
      builder.phoneNumber(dto.phone());
    }
    if (dto.dob() != null) {
      builder.dob(dto.dob());
    }
    if (StringUtils.isNotBlank(dto.gender())) {
      builder.gender(parseGender(dto.gender()));
    }

    return builder.build();
  }

  /**
   * Convert UpdateUserRequestDto to UpdateUserRequest gRPC
   */
  default UpdateUserRequest toUpdateUserRequestGrpc(UpdateUserRequestDto dto) {
    if (dto == null) {
      return null;
    }

    UpdateUserRequest.Builder builder = UpdateUserRequest.newBuilder();
    builder.setId(dto.id());
    if (dto.fullName() != null) {
      builder.setFullName(dto.fullName());
    }
    if (dto.email() != null) {
      builder.setEmail(dto.email());
    }
    if (dto.phone() != null) {
      builder.setPhone(dto.phone());
    }
    if (dto.dob() != null) {
      builder.setDob(localDateToProtoDate(dto.dob()));
    }
    if (dto.gender() != null) {
      builder.setGender(dto.gender());
    }
    return builder.build();
  }

  // =====================================================
  // LOGOUT MAPPING
  // =====================================================

  /**
   * Convert LogoutRequestDto to LogoutRequest gRPC
   */
  default LogoutRequest toLogoutRequestGrpc(LogoutRequestDto dto) {
    if (dto == null) {
      return null;
    }
    LogoutRequest.Builder builder = LogoutRequest.newBuilder();
    if (dto.userId() != null) {
      builder.setUserId(dto.userId());
    }
    if (dto.accessToken() != null) {
      builder.setAccessToken(dto.accessToken());
    }
    return builder.build();
  }

  /**
   * Convert LogoutResponse gRPC to LogoutResponseDto
   */
  default LogoutResponseDto toLogoutResponseDto(LogoutResponse grpc) {
    if (grpc == null) {
      return null;
    }
    return new LogoutResponseDto(grpc.getSuccess(), grpc.getMessage());
  }

  // =====================================================
  // FORGOT PASSWORD MAPPING
  // =====================================================

  /**
   * Convert ForgotPasswordRequestDto to ForgotPasswordRequest gRPC
   */
  default ForgotPasswordRequest toForgotPasswordRequestGrpc(ForgotPasswordRequestDto dto) {
    if (dto == null) {
      return null;
    }
    return ForgotPasswordRequest.newBuilder()
        .setPhoneOrEmail(dto.phoneOrEmail())
        .build();
  }

  /**
   * Convert ForgotPasswordResponse gRPC to ForgotPasswordResponseDto
   */
  default ForgotPasswordResponseDto toForgotPasswordResponseDto(ForgotPasswordResponse grpc) {
    if (grpc == null) {
      return null;
    }
    return new ForgotPasswordResponseDto(
        grpc.getSuccess(),
        grpc.getMessage(),
        grpc.getResetToken(),
        grpc.getExpiresInSeconds()
    );
  }

  // =====================================================
  // CHANGE PASSWORD MAPPING
  // =====================================================

  /**
   * Convert ChangePasswordRequestDto to ChangePasswordRequest gRPC
   */
  default ChangePasswordRequest toChangePasswordRequestGrpc(ChangePasswordRequestDto dto) {
    if (dto == null) {
      return null;
    }
    return ChangePasswordRequest.newBuilder()
        .setResetToken(dto.resetToken())
        .setNewPassword(dto.newPassword())
        .setConfirmPassword(dto.confirmPassword())
        .build();
  }

  /**
   * Convert ChangePasswordResponse gRPC to ChangePasswordResponseDto
   */
  default ChangePasswordResponseDto toChangePasswordResponseDto(ChangePasswordResponse grpc) {
    if (grpc == null) {
      return null;
    }
    return new ChangePasswordResponseDto(grpc.getSuccess(), grpc.getMessage());
  }

  // =====================================================
  // REFRESH TOKEN MAPPING
  // =====================================================

  /**
   * Convert RefreshTokenRequestDto to RefreshTokenRequest gRPC
   */
  default RefreshTokenRequest toRefreshTokenRequestGrpc(RefreshTokenRequestDto dto) {
    if (dto == null) {
      return null;
    }
    return RefreshTokenRequest.newBuilder()
        .setRefreshToken(dto.refreshToken())
        .build();
  }

  /**
   * Convert RefreshTokenResponse gRPC to RefreshTokenResponseDto
   */
  default RefreshTokenResponseDto toRefreshTokenResponseDto(RefreshTokenResponse grpc) {
    if (grpc == null) {
      return null;
    }
    return new RefreshTokenResponseDto(
        grpc.getAccessToken(),
        grpc.getRefreshToken(),
        grpc.getTokenType(),
        grpc.getExpiresIn(),
        grpc.getUserId()
    );
  }
}
