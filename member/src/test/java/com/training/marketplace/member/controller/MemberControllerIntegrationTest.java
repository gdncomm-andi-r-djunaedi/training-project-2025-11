package com.training.marketplace.member.controller;

import com.training.marketplace.member.controller.modal.request.LoginRequest;
import com.training.marketplace.member.controller.modal.request.LoginResponse;
import com.training.marketplace.member.controller.modal.request.LogoutRequest;
import com.training.marketplace.member.controller.modal.request.RegisterRequest;
import com.training.marketplace.member.controller.modal.response.DefaultMemberResponse;
import com.training.marketplace.member.service.MemberServiceGrpc;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.grpc.client.ImportGrpcClients;

import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest(properties = {
        "grpc.server.inProcessName=test", // Enable in-process server for tests
        "grpc.server.port=-1", // Disable external server
        "grpc.client.inProcess.name=test" // Configure the client to connect to the in-process server
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MemberControllerIntegrationTest {

    @Autowired
    private MemberServiceGrpc.MemberServiceBlockingStub memberServiceBlockingStub;

    private static String authToken;
    private static String memberId;

    @Test
    @Order(1)
    public void testRegister(){
        RegisterRequest request = RegisterRequest.newBuilder()
                .setUsername("testuser")
                .setPassword("password")
                .build();

        DefaultMemberResponse response = memberServiceBlockingStub.register(request);
        assertTrue(response.getSuccess());

        DefaultMemberResponse response2 = memberServiceBlockingStub.register(request);
        assertFalse(response2.getSuccess());

    }

    @Test
    @Order(2)
    public void testLogin(){
        LoginRequest request = LoginRequest.newBuilder()
                .setUsername("testuser")
                .setPassword("password")
                .build();
        LoginResponse response = memberServiceBlockingStub.login(request);
        assertNotNull(response.getAuthToken());
        assertNotNull(response.getRefreshToken());
        assertNotNull(response.getMemberId());
        authToken = response.getAuthToken();
        memberId = response.getMemberId();
    }

    @Test
    @Order(3)
    public void testLogout(){
        LogoutRequest request = LogoutRequest.newBuilder()
                .setMemberId(memberId)
                .setAuthToken(authToken)
                .build();

        DefaultMemberResponse response = memberServiceBlockingStub.logout(request);
        assertTrue(response.getSuccess());
    }
}
