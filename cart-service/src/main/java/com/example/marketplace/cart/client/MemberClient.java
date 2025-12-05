package com.example.marketplace.cart.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "member-service", url = "${services.member.url}")
public interface MemberClient {

    @GetMapping("/internal/members/{id}/exists")
    Boolean exists(@PathVariable("id") String id);
}
