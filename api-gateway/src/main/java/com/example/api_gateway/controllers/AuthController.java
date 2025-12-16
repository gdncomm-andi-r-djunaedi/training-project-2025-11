package com.example.api_gateway.controllers;

//import com.example.api_gateway.service.TokenBlacklistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
public class AuthController {

    @PostMapping("/auth/logout")
    public Mono<ResponseEntity<Void>> logout(ServerWebExchange exchange){

        ResponseCookie cookie = ResponseCookie.from("Authorization", "")
                .httpOnly(true)
                .maxAge(0)
                .path("/")
                .build();
        
        exchange.getResponse().addCookie(cookie);
        return Mono.just(ResponseEntity.ok().build());
    }
}
