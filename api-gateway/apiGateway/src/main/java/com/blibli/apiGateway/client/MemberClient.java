package com.blibli.apiGateway.client;


import com.blibli.apiGateway.dto.MemberProfileDTO;
import com.blibli.apiGateway.dto.UserLoginRequestDTO;
import com.blibli.apiGateway.dto.UserRegisterDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "memberService", url = "${member.service.url}")
public interface MemberClient {

    @PostMapping("/api/members/register")
    UserRegisterDTO register(@RequestBody UserRegisterDTO userRegisterDTO);

    @PostMapping("/api/members/validateMember")
    boolean validateMember(@RequestBody UserLoginRequestDTO userLoginRequestDTO);

    @GetMapping("/api/members/getUserProfile")
    ResponseEntity<MemberProfileDTO> getUserProfile(@RequestHeader("Authorization") String authToken);
}
