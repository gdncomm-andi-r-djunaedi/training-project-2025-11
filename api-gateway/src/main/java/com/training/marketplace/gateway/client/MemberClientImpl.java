package com.training.marketplace.gateway.client;

import com.training.marketplace.cart.modal.response.DefaultCartResponse;
import com.training.marketplace.member.controller.modal.request.LoginRequest;
import com.training.marketplace.member.controller.modal.request.LoginResponse;
import com.training.marketplace.member.controller.modal.request.LogoutRequest;
import com.training.marketplace.member.controller.modal.request.RegisterRequest;
import com.training.marketplace.member.controller.modal.response.DefaultMemberResponse;
import com.training.marketplace.member.service.MemberServiceGrpc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.grpc.client.ImportGrpcClients;

@ImportGrpcClients(target = "member", prefix = "member")
public class MemberClientImpl {

    @Autowired
    private MemberServiceGrpc.MemberServiceBlockingStub memberSvcStub;

    public DefaultMemberResponse register(RegisterRequest request){
        return memberSvcStub.register(request);
    }

    public LoginResponse login(LoginRequest request){
        return memberSvcStub.login(request);
    }

    public DefaultMemberResponse logout(LogoutRequest request){
        return memberSvcStub.logout(request);
    }
}

