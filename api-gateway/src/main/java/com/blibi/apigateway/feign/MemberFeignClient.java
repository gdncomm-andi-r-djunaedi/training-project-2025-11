package com.blibi.apigateway.feign;

import com.blibi.apigateway.dto.GenericResponse;
import com.blibi.apigateway.dto.LoginRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "member-service", url = "http://localhost:8081/api/v1/member")
public interface MemberFeignClient {
    @PostMapping("/login")
    GenericResponse<?> login(LoginRequest request);
}
