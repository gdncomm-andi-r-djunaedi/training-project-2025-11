package com.wijaya.commerce.member.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wijaya.commerce.member.command.CommandExecutor;
import com.wijaya.commerce.member.constant.MemberApiPath;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MemberController {
    private final CommandExecutor commandExecutor;

    @GetMapping(MemberApiPath.REGISTER)
    public RegisterResponseWebModel register(
        @Valid @RequestBody RegisterRequestWebModel request) {
            commandExecutor.execute(RegisterCommand.class, null)
        return commandExecutor.execute(RegisterCommand.class, RegisterCommandRequest.builder().build());
    }

}
