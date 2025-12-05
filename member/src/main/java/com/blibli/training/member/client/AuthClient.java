package com.blibli.training.member.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "api-gateway", url = "http://localhost:8080")
public interface AuthClient {

    @PostMapping("/auth/generate-token")
    String generateToken(@RequestParam("username") String username);

    @PostMapping("/auth/logout")
    void logout(@RequestParam("token") String token);
}
