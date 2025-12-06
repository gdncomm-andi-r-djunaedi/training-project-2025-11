package com.training.marketplace.member.service;

import com.training.marketplace.member.controller.modal.request.LoginRequest;
import com.training.marketplace.member.controller.modal.request.LoginResponse;
import com.training.marketplace.member.controller.modal.request.LogoutRequest;
import com.training.marketplace.member.controller.modal.request.RegisterRequest;
import com.training.marketplace.member.controller.modal.response.DefaultMemberResponse;
import com.training.marketplace.member.entity.MemberEntity;
import com.training.marketplace.member.repository.MemberRepository;
import com.training.marketplace.member.repository.UserTokenRepository;
import com.training.marketplace.member.utils.JwtUtils;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.grpc.server.service.GrpcService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@GrpcService
public class MemberServiceImpl extends MemberServiceGrpc.MemberServiceImplBase {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserTokenRepository userTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public void register(RegisterRequest request, StreamObserver<DefaultMemberResponse> responseObserver) {
        this.memberRepository.save(MemberEntity.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("ROLE_CUSTOMER")
                .build());
        DefaultMemberResponse response = DefaultMemberResponse.newBuilder().setSuccess(true).setMessage("OK").build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        LoginResponse response = LoginResponse.newBuilder()
                .setMemberId(String.valueOf(memberRepository.findUserByUsername(request.getUsername())))
                .setAuthToken(jwtUtils.generateAccessToken(authentication))
                .setRefreshToken(jwtUtils.generateRefreshToken(authentication))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void logout(LogoutRequest request, StreamObserver<DefaultMemberResponse> responseObserver) {
        super.logout(request, responseObserver);
    }

}
