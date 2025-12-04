package com.microservice.api_gateway.client;

import com.microservice.api_gateway.dto.LoginRequestDto;
import com.microservice.api_gateway.dto.MemberServiceResponseWrapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "member-service", url = "http://localhost:8081")
public interface LogInFeign {

    @PostMapping("/api/member/logIn")
    MemberServiceResponseWrapper logIn(@RequestBody LoginRequestDto loginRequest);
}