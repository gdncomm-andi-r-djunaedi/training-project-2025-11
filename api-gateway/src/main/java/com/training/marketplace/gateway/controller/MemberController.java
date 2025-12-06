package com.training.marketplace.gateway.controller;

import com.training.marketplace.gateway.service.MemberClientService;
import com.training.marketplace.member.controller.modal.request.LoginRequest;
import com.training.marketplace.member.controller.modal.request.LoginResponse;
import com.training.marketplace.member.controller.modal.request.LogoutRequest;
import com.training.marketplace.member.controller.modal.request.RegisterRequest;
import com.training.marketplace.member.controller.modal.response.DefaultMemberResponse;
import com.training.marketplace.gateway.dto.member.DefaultMemberResponseDTO;
import com.training.marketplace.gateway.dto.member.RegisterRequestDTO;
import com.training.marketplace.gateway.dto.member.LoginResponseDTO;
import com.training.marketplace.gateway.dto.member.LoginRequestDTO;
import com.training.marketplace.gateway.dto.member.LogoutRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/member")
public class MemberController {

    @Autowired
    private MemberClientService memberClient;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, path = "/register")
    public DefaultMemberResponseDTO register(@RequestBody RegisterRequestDTO request) {
        log.info(String.format("registering member %s", request.getUsername()));
        DefaultMemberResponse response = memberClient.register(RegisterRequest.newBuilder()
                .setUsername(request.getUsername())
                .setPassword(request.getPassword())
                .build());
        return DefaultMemberResponseDTO.builder()
                .success(response.getSuccess())
                .message(response.getMessage())
                .build();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, path = "/login")
    public LoginResponseDTO login(@RequestBody LoginRequestDTO request) {
        log.info(String.format("logging in member %s", request.getUsername()));
        LoginResponse response = memberClient.login(LoginRequest.newBuilder()
                .setUsername(request.getUsername())
                .setPassword(request.getPassword())
                .build());
        return LoginResponseDTO.builder()
                .memberId(response.getMemberId())
                .authToken(response.getAuthToken())
                .refreshToken(response.getRefreshToken())
                .build();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, path = "/logout")
    public DefaultMemberResponseDTO logout(@RequestBody LogoutRequestDTO request) {
        log.info(String.format("logging out member %s", request.getMemberId()));
        DefaultMemberResponse response = memberClient.logout(LogoutRequest.newBuilder()
                .setMemberId(request.getMemberId())
                .setAuthToken(request.getAuthToken())
                .build());
        return DefaultMemberResponseDTO.builder()
                .success(response.getSuccess())
                .message(response.getMessage())
                .build();
    }

}
