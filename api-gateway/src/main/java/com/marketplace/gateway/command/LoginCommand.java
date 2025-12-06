package com.marketplace.gateway.command;

import com.marketplace.common.command.ReactiveCommand;
import com.marketplace.gateway.dto.LoginRequest;
import com.marketplace.gateway.dto.LoginResponse;

public interface LoginCommand extends ReactiveCommand<LoginRequest, LoginResponse> {
}
