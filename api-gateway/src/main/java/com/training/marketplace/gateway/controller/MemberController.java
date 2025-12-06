package com.training.marketplace.gateway.controller;

import com.training.marketplace.gateway.client.MemberClientImpl;
import com.training.marketplace.member.controller.modal.request.LoginRequest;
import com.training.marketplace.member.controller.modal.request.LoginResponse;
import com.training.marketplace.member.controller.modal.request.LogoutRequest;
import com.training.marketplace.member.controller.modal.request.RegisterRequest;
import com.training.marketplace.member.controller.modal.response.DefaultMemberResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/member")
public class MemberController {
    @Autowired
    private MemberClientImpl memberClient;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, path = "/register")
    public DefaultMemberResponse register(@RequestBody RegisterRequest request){
        return memberClient.register(request);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, path = "/login")
    public LoginResponse login(@RequestBody LoginRequest request){
        return memberClient.login(request);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, path = "/logout")
    public DefaultMemberResponse logout(@RequestBody LogoutRequest request){
        return memberClient.logout(request);
    }

}
