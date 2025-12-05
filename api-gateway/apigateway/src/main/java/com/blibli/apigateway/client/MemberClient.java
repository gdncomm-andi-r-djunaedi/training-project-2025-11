package com.blibli.apigateway.client;

import com.blibli.apigateway.dto.response.CheckUserResponse;
import com.blibli.apigateway.dto.request.MemberDto;
import com.blibli.apigateway.dto.request.ValidateMemberRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
    name = "member-service", 
    url = "${member.service.url}"
)
public interface MemberClient {

    @PostMapping("/api/members/register")
    MemberDto register(@RequestBody MemberDto memberDto);

    @PostMapping("/api/members/validateMember")
    CheckUserResponse validateMember(@RequestBody ValidateMemberRequest validateMemberRequest);

    @GetMapping("/api/members/getMemberDetails")
    MemberDto getMemberDetails(@RequestHeader("Authorization") String authHeader);
}
