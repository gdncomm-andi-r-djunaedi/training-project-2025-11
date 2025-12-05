package com.gdn.project.waroenk.member.controller.http;

import com.gdn.project.waroenk.common.Id;
import com.gdn.project.waroenk.common.SortBy;
import com.gdn.project.waroenk.member.AuthenticateRequest;
import com.gdn.project.waroenk.member.ChangePasswordRequest;
import com.gdn.project.waroenk.member.ChangePasswordResponse;
import com.gdn.project.waroenk.member.CreateUserRequest;
import com.gdn.project.waroenk.member.CreateUserResponse;
import com.gdn.project.waroenk.member.FilterUserRequest;
import com.gdn.project.waroenk.member.ForgotPasswordRequest;
import com.gdn.project.waroenk.member.ForgotPasswordResponse;
import com.gdn.project.waroenk.member.LogoutRequest;
import com.gdn.project.waroenk.member.LogoutResponse;
import com.gdn.project.waroenk.member.MultipleUserResponse;
import com.gdn.project.waroenk.member.PhoneOrEmailRequest;
import com.gdn.project.waroenk.member.RefreshTokenRequest;
import com.gdn.project.waroenk.member.RefreshTokenResponse;
import com.gdn.project.waroenk.member.UpdateUserRequest;
import com.gdn.project.waroenk.member.UserData;
import com.gdn.project.waroenk.member.UserServiceGrpc;
import com.gdn.project.waroenk.member.UserTokenResponse;
import com.gdn.project.waroenk.member.constant.ApiConstant;
import com.gdn.project.waroenk.member.constant.Sort;
import com.gdn.project.waroenk.member.dto.AuthenticateRequestDto;
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
import com.gdn.project.waroenk.member.mapper.UserMapper;
import jakarta.validation.Valid;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(ApiConstant.BASE_PATH)
@RestController("memberHttpUserController")
public class UserController {

  private static final UserMapper mapper = UserMapper.INSTANCE;
  private final UserServiceGrpc.UserServiceBlockingStub grpcClient;

  @Autowired
  public UserController(@GrpcClient("member-service") UserServiceGrpc.UserServiceBlockingStub grpcClient) {
    this.grpcClient = grpcClient;
  }

  @PostMapping("/user/register")
  public UserResponseDto register(@Valid @RequestBody CreateUserRequestDto requestDto) {
    CreateUserRequest request = mapper.toCreateUserRequestGrpc(requestDto);
    CreateUserResponse response = grpcClient.register(request);
    return mapper.toUserResponseDto(response);
  }

  @PostMapping("/user/login")
  public UserTokenResponseDto login(@Valid @RequestBody AuthenticateRequestDto requestDto) {
    AuthenticateRequest request = AuthenticateRequest.newBuilder()
        .setUser(requestDto.user())
        .setPassword(requestDto.password())
        .build();
    UserTokenResponse response = grpcClient.authenticate(request);
    return mapper.toUserTokenResponseDto(response);
  }

  @PutMapping("/user")
  public UserResponseDto updateUser(@Valid @RequestBody UpdateUserRequestDto requestDto) {
    UpdateUserRequest request = mapper.toUpdateUserRequestGrpc(requestDto);
    UserData response = grpcClient.updateUser(request);
    return mapper.toUserResponseDto(response);
  }

  @GetMapping("/user")
  public UserResponseDto findUserById(@RequestParam String id) {
    Id request = Id.newBuilder().setValue(id).build();
    UserData response = grpcClient.getOneUserById(request);
    return mapper.toUserResponseDto(response);
  }

  @GetMapping("/user/find-one")
  public UserResponseDto findUserByPhoneOrEmail(@RequestParam String phoneOrEmail) {
    PhoneOrEmailRequest request = PhoneOrEmailRequest.newBuilder()
        .setPhoneOrEmail(phoneOrEmail)
        .build();
    UserData response = grpcClient.getOneUserByPhoneOrEmail(request);
    return mapper.toUserResponseDto(response);
  }

  @GetMapping("/user/filter")
  public ListOfUserResponseDto filterUsers(
      @RequestParam(required = false) String query,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false, defaultValue = "id") String sortBy,
      @RequestParam(required = false, defaultValue = "asc") Sort sortDirection) {

    FilterUserRequest.Builder requestBuilder = FilterUserRequest.newBuilder();
    requestBuilder.setSize(size);

    if (StringUtils.isNotBlank(query)) {
      requestBuilder.setQuery(query);
    }
    if (StringUtils.isNotBlank(cursor)) {
      requestBuilder.setCursor(cursor);
    }
    if (StringUtils.isNotBlank(sortBy)) {
      SortBy.Builder sortBuilder = SortBy.newBuilder();
      sortBuilder.setField(sortBy);
      if (ObjectUtils.isNotEmpty(sortDirection)) {
        sortBuilder.setDirection(sortDirection.getShortName());
      }
      requestBuilder.setSortBy(sortBuilder.build());
    }

    MultipleUserResponse response = grpcClient.filterUser(requestBuilder.build());
    return mapper.toListOfUserResponseDto(response);
  }

  @PostMapping("/user/logout")
  public LogoutResponseDto logout(@Valid @RequestBody LogoutRequestDto requestDto) {
    LogoutRequest request = mapper.toLogoutRequestGrpc(requestDto);
    LogoutResponse response = grpcClient.logout(request);
    return mapper.toLogoutResponseDto(response);
  }

  @PostMapping("/user/forgot-password")
  public ForgotPasswordResponseDto forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto requestDto) {
    ForgotPasswordRequest request = mapper.toForgotPasswordRequestGrpc(requestDto);
    ForgotPasswordResponse response = grpcClient.forgotPassword(request);
    return mapper.toForgotPasswordResponseDto(response);
  }

  @PostMapping("/user/change-password")
  public ChangePasswordResponseDto changePassword(@Valid @RequestBody ChangePasswordRequestDto requestDto) {
    ChangePasswordRequest request = mapper.toChangePasswordRequestGrpc(requestDto);
    ChangePasswordResponse response = grpcClient.changePassword(request);
    return mapper.toChangePasswordResponseDto(response);
  }

  @PostMapping("/user/refresh-token")
  public RefreshTokenResponseDto refreshToken(@Valid @RequestBody RefreshTokenRequestDto requestDto) {
    RefreshTokenRequest request = mapper.toRefreshTokenRequestGrpc(requestDto);
    RefreshTokenResponse response = grpcClient.refreshToken(request);
    return mapper.toRefreshTokenResponseDto(response);
  }
}
