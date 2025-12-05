package com.blibli.training.cart.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "member-service", url = "http://localhost:8080")
public interface MemberClient {
    @GetMapping("/member/api/members/username/{username}")
    Object findByUsername(@PathVariable("username") String username);
}
