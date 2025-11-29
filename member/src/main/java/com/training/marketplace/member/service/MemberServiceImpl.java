package com.training.marketplace.member.service;

import com.training.marketplace.member.controller.modal.request.LoginRequest;
import com.training.marketplace.member.controller.modal.request.LoginResponse;
import com.training.marketplace.member.controller.modal.request.LogoutRequest;
import com.training.marketplace.member.controller.modal.request.RegisterRequest;
import com.training.marketplace.member.controller.modal.response.DefaultResponse;
import com.training.marketplace.member.entity.MemberEntity;
import com.training.marketplace.member.repository.MemberRepository;
import com.training.marketplace.member.repository.UserTokenRepository;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.grpc.server.service.GrpcService;
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

    @Override
    public void register(RegisterRequest request, StreamObserver<DefaultResponse> responseObserver) {
        this.memberRepository.save(MemberEntity.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("ROLE_CUSTOMER")
                .build());
        DefaultResponse response = DefaultResponse.newBuilder().setMessage("OK").build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
        this.memberRepository.findUserByUsername(request.getUsername());

//        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void logout(LogoutRequest request, StreamObserver<DefaultResponse> responseObserver) {
        super.logout(request, responseObserver);
    }

}
