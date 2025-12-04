package com.marketplace.gateway.command;

import com.marketplace.common.command.ReactiveCommand;
import com.marketplace.gateway.dto.LoginRequest;
import com.marketplace.gateway.dto.LoginResponse;
import com.marketplace.gateway.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class LoginCommand implements ReactiveCommand<LoginRequest, LoginResponse> {

    private final AuthService authService;

    @Override
    public Mono<LoginResponse> execute(LoginRequest request) {
        return authService.login(request);
    }
}
