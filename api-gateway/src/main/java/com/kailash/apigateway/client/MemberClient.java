package com.kailash.apigateway.client;

import com.kailash.apigateway.dto.ApiResponse;
import com.kailash.apigateway.dto.LoginRequest;
import com.kailash.apigateway.dto.MemberResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "member",url = "http://localhost:8085")
public interface MemberClient {
    @PostMapping("/login")
    ResponseEntity<ApiResponse<MemberResponse>> login(@RequestBody LoginRequest req);
}
