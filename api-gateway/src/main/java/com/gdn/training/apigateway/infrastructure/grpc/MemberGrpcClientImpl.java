package com.gdn.training.apigateway.infrastructure.grpc;

import org.springframework.stereotype.Component;

import com.gdn.training.apigateway.application.port.MemberGrpcPort;
import com.gdn.training.apigateway.application.usecase.model.LoginResult;
import com.gdn.training.apigateway.application.usecase.model.MemberProfile;
import com.gdn.training.apigateway.application.usecase.model.RegisterResult;

import com.gdn.training.member.infrastructure.grpc.proto.*;
import io.grpc.ManagedChannel;

@Component
public class MemberGrpcClientImpl implements MemberGrpcPort {

    private final MemberServiceGrpc.MemberServiceBlockingStub stub;

    public MemberGrpcClientImpl(ManagedChannel channel) {
        this.stub = MemberServiceGrpc.newBlockingStub(channel);
    }

    @Override
    public RegisterResult register(String fullName, String email, String password, String phoneNumber) {

        RegisterMemberRequest req = RegisterMemberRequest.newBuilder()
                .setFullName(fullName)
                .setEmail(email)
                .setRawPassword(password)
                .setPhoneNumber(phoneNumber)
                .build();

        RegisterMemberResponse res = stub.registerMember(req);

        return new RegisterResult(
                res.getMemberId(),
                res.getFullName(),
                res.getEmail());
    }

    @Override
    public LoginResult login(String email, String password) {

        LoginMemberRequest req = LoginMemberRequest.newBuilder()
                .setEmail(email)
                .setRawPassword(password)
                .build();

        LoginMemberResponse res = stub.loginMember(req);

        return new LoginResult(
                res.getSuccess(),
                res.getMemberId(),
                res.getFullName(),
                res.getEmail());
    }

    @Override
    public boolean validateMember(String memberId) {
        ValidateMemberRequest req = ValidateMemberRequest.newBuilder()
                .setMemberId(memberId)
                .build();

        ValidateMemberResponse res = stub.validateMember(req);
        return res.getExists();
    }

    @Override
    public void logout() {
        stub.logoutMember(Empty.getDefaultInstance());
    }

    @Override
    public MemberProfile getMemberProfile(String memberId) {

        GetMemberProfileRequest req = GetMemberProfileRequest.newBuilder()
                .setMemberId(memberId)
                .build();

        GetMemberProfileResponse res = stub.getMemberProfile(req);

        var m = res.getMember();

        return new MemberProfile(
                m.getId(),
                m.getEmail(),
                m.getFullName(),
                m.getPhoneNumber(),
                null,
                null,
                null);
    }
}