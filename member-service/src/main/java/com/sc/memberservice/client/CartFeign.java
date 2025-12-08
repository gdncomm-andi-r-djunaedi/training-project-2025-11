package com.sc.memberservice.client;

import com.sc.utilsservice.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "cart-service", url = "http://localhost:8093/cart")
public interface CartFeign {

    @PostMapping("/create")
    ResponseEntity<ApiResponse<String>> createCart(@RequestParam String memberId);
}
